import { defineStore } from 'pinia'
import type { CartItemResponse, OrderItemRequest, OrderPreviewResponse } from '@/api/student'

export type StudentCartItem = CartItemResponse
export type StudentCheckoutDraft = {
  storeId: number
  items: OrderItemRequest[]
  receiverName?: string
  receiverPhone?: string
  receiverAddress?: string
  userCouponId?: number
  remark: string
}

type CartGroup = {
  storeId: number
  items: StudentCartItem[]
}

export const useStudentCheckoutStore = defineStore('student-checkout', {
  state: () => ({
    cartItems: [] as StudentCartItem[],
    checkoutDraft: null as StudentCheckoutDraft | null,
    previewResult: null as OrderPreviewResponse | null,
    submitting: false,
    paying: false,
  }),
  getters: {
    totalCount: (state) => state.cartItems.reduce((sum, item) => sum + Number(item.quantity ?? 0), 0),
    cartGroups: (state): CartGroup[] => {
      const groups = new Map<number, StudentCartItem[]>()

      state.cartItems.forEach((item) => {
        const group = groups.get(item.storeId)
        if (group) {
          group.push(item)
          return
        }
        groups.set(item.storeId, [item])
      })

      return Array.from(groups.entries()).map(([storeId, items]) => ({
        storeId,
        items,
      }))
    },
  },
  actions: {
    invalidateCheckoutState() {
      this.checkoutDraft = null
      this.previewResult = null
    },
    setCartItems(items: StudentCartItem[]) {
      this.cartItems = items
      this.invalidateCheckoutState()
    },
    updateCartItemQuantity(id: number, quantity: number) {
      this.cartItems = this.cartItems.map((item) =>
        item.id === id
          ? {
              ...item,
              quantity,
              totalAmount: Number(item.price ?? 0) * quantity,
            }
          : item,
      )
      this.invalidateCheckoutState()
    },
    removeCartItem(id: number) {
      this.cartItems = this.cartItems.filter((item) => item.id !== id)
      this.invalidateCheckoutState()
    },
    clearStoreCart(storeId: number) {
      this.cartItems = this.cartItems.filter((item) => item.storeId !== storeId)
      this.invalidateCheckoutState()
    },
    beginCheckout(storeId: number) {
      const selectedItems = this.cartItems.filter((item) => item.storeId === storeId)

      this.checkoutDraft = {
        storeId,
        items: selectedItems.map((item) => ({
          skuId: item.skuId,
          quantity: item.quantity,
        })),
        receiverName: '',
        receiverPhone: '',
        receiverAddress: '',
        userCouponId: undefined,
        remark: '',
      }
      this.previewResult = null
    },
    fillReceiver(address: { contactName: string; contactPhone: string; fullAddress: string }) {
      if (!this.checkoutDraft) {
        return
      }

      this.checkoutDraft = {
        ...this.checkoutDraft,
        receiverName: address.contactName,
        receiverPhone: address.contactPhone,
        receiverAddress: address.fullAddress,
      }
    },
    setCoupon(userCouponId?: number) {
      if (!this.checkoutDraft) {
        return
      }

      this.checkoutDraft = {
        ...this.checkoutDraft,
        userCouponId,
      }
    },
    setRemark(remark: string) {
      if (!this.checkoutDraft) {
        return
      }

      this.checkoutDraft = {
        ...this.checkoutDraft,
        remark,
      }
    },
  },
})
