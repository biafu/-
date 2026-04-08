import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import type { OrderCreateResponse, OrderDetailResponse, OrderPreviewRequest } from '@/api/student'
import { useStudentCheckoutStore, type StudentCheckoutDraft } from './student-checkout'

type Assert<T extends true> = T
type IsExact<A, B> =
  (<T>() => T extends A ? 1 : 2) extends
  (<T>() => T extends B ? 1 : 2)
    ? (<T>() => T extends B ? 1 : 2) extends (<T>() => T extends A ? 1 : 2)
      ? true
      : false
    : false

const orderDetailOrderNoIsString: Assert<IsExact<OrderDetailResponse['orderNo'], string>> = true
const orderCreateOrderNoIsString: Assert<IsExact<OrderCreateResponse['orderNo'], string>> = true
const checkoutDraftTypeIsDistinctFromRequest: Assert<
  IsExact<StudentCheckoutDraft, OrderPreviewRequest> extends true ? false : true
> = true

void orderDetailOrderNoIsString
void orderCreateOrderNoIsString
void checkoutDraftTypeIsDistinctFromRequest

describe('student checkout store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('builds a checkout draft from cart items in one store', () => {
    const store = useStudentCheckoutStore()

    store.setCartItems([
      {
        id: 1,
        storeId: 101,
        skuId: 1001,
        productName: 'Braised Chicken Rice',
        skuName: 'Large',
        price: 18,
        quantity: 2,
        totalAmount: 36,
      },
      {
        id: 2,
        storeId: 101,
        skuId: 1002,
        productName: 'Lemon Tea',
        skuName: 'Standard',
        price: 6,
        quantity: 1,
        totalAmount: 6,
      },
      {
        id: 3,
        storeId: 202,
        skuId: 2001,
        productName: 'Spicy Noodles',
        skuName: 'Regular',
        price: 14,
        quantity: 3,
        totalAmount: 42,
      },
    ])

    store.beginCheckout(101)

    expect(store.checkoutDraft).toEqual({
      storeId: 101,
      items: [
        { skuId: 1001, quantity: 2 },
        { skuId: 1002, quantity: 1 },
      ],
      receiverName: '',
      receiverPhone: '',
      receiverAddress: '',
      userCouponId: undefined,
      remark: '',
    })
  })

  it('groups cart items by store for checkout sections', () => {
    const store = useStudentCheckoutStore()

    store.setCartItems([
      {
        id: 1,
        storeId: 101,
        skuId: 1001,
        productName: 'Braised Chicken Rice',
        skuName: 'Large',
        price: 18,
        quantity: 2,
        totalAmount: 36,
      },
      {
        id: 2,
        storeId: 202,
        skuId: 2001,
        productName: 'Spicy Noodles',
        skuName: 'Regular',
        price: 14,
        quantity: 3,
        totalAmount: 42,
      },
      {
        id: 3,
        storeId: 101,
        skuId: 1002,
        productName: 'Lemon Tea',
        skuName: 'Standard',
        price: 6,
        quantity: 1,
        totalAmount: 6,
      },
    ])

    expect(store.cartGroups).toEqual([
      {
        storeId: 101,
        items: [
          {
            id: 1,
            storeId: 101,
            skuId: 1001,
            productName: 'Braised Chicken Rice',
            skuName: 'Large',
            price: 18,
            quantity: 2,
            totalAmount: 36,
          },
          {
            id: 3,
            storeId: 101,
            skuId: 1002,
            productName: 'Lemon Tea',
            skuName: 'Standard',
            price: 6,
            quantity: 1,
            totalAmount: 6,
          },
        ],
      },
      {
        storeId: 202,
        items: [
          {
            id: 2,
            storeId: 202,
            skuId: 2001,
            productName: 'Spicy Noodles',
            skuName: 'Regular',
            price: 14,
            quantity: 3,
            totalAmount: 42,
          },
        ],
      },
    ])
  })

  it('tracks the total cart item count for the navigation badge', () => {
    const store = useStudentCheckoutStore()

    store.setCartItems([
      {
        id: 1,
        storeId: 101,
        skuId: 1001,
        productName: 'Braised Chicken Rice',
        skuName: 'Large',
        price: 18,
        quantity: 2,
        totalAmount: 36,
      },
      {
        id: 2,
        storeId: 202,
        skuId: 2001,
        productName: 'Spicy Noodles',
        skuName: 'Regular',
        price: 14,
        quantity: 3,
        totalAmount: 42,
      },
    ])

    expect(store.totalCount).toBe(5)
  })

  it('resets any preview result when a new checkout begins', () => {
    const store = useStudentCheckoutStore()

    store.setCartItems([
      {
        id: 1,
        storeId: 101,
        skuId: 1001,
        productName: 'Braised Chicken Rice',
        skuName: 'Large',
        price: 18,
        quantity: 2,
        totalAmount: 36,
      },
    ])

    store.previewResult = {
      goodsAmount: 36,
      deliveryFee: 3,
      discountAmount: 0,
      payAmount: 39,
      items: [
        {
          skuId: 1001,
          productName: 'Braised Chicken Rice',
          skuName: 'Large',
          quantity: 2,
          price: 18,
          totalAmount: 36,
        },
      ],
    }

    store.beginCheckout(101)

    expect(store.previewResult).toBeNull()
  })

  it('updates local cart quantities and removals for cart page interactions', () => {
    const store = useStudentCheckoutStore()

    store.setCartItems([
      {
        id: 1,
        storeId: 101,
        skuId: 1001,
        productName: 'Braised Chicken Rice',
        skuName: 'Large',
        price: 18,
        quantity: 2,
        totalAmount: 36,
      },
      {
        id: 2,
        storeId: 101,
        skuId: 1002,
        productName: 'Lemon Tea',
        skuName: 'Standard',
        price: 6,
        quantity: 1,
        totalAmount: 6,
      },
      {
        id: 3,
        storeId: 202,
        skuId: 2001,
        productName: 'Spicy Noodles',
        skuName: 'Regular',
        price: 14,
        quantity: 3,
        totalAmount: 42,
      },
    ])

    store.beginCheckout(101)
    store.previewResult = {
      goodsAmount: 42,
      deliveryFee: 3,
      discountAmount: 0,
      payAmount: 45,
      items: [
        {
          skuId: 1001,
          productName: 'Braised Chicken Rice',
          skuName: 'Large',
          quantity: 2,
          price: 18,
          totalAmount: 36,
        },
        {
          skuId: 1002,
          productName: 'Lemon Tea',
          skuName: 'Standard',
          quantity: 1,
          price: 6,
          totalAmount: 6,
        },
      ],
    }

    store.updateCartItemQuantity(1, 4)
    store.removeCartItem(2)
    store.clearStoreCart(202)

    expect(store.checkoutDraft).toBeNull()
    expect(store.previewResult).toBeNull()
    expect(store.cartItems).toEqual([
      {
        id: 1,
        storeId: 101,
        skuId: 1001,
        productName: 'Braised Chicken Rice',
        skuName: 'Large',
        price: 18,
        quantity: 4,
        totalAmount: 72,
      },
    ])
    expect(store.totalCount).toBe(4)
    expect(store.cartGroups).toEqual([
      {
        storeId: 101,
        items: [
          {
            id: 1,
            storeId: 101,
            skuId: 1001,
            productName: 'Braised Chicken Rice',
            skuName: 'Large',
            price: 18,
            quantity: 4,
            totalAmount: 72,
          },
        ],
      },
    ])
  })

  it('clears stale checkout state when cart items are replaced', () => {
    const store = useStudentCheckoutStore()

    store.setCartItems([
      {
        id: 1,
        storeId: 101,
        skuId: 1001,
        productName: 'Braised Chicken Rice',
        skuName: 'Large',
        price: 18,
        quantity: 2,
        totalAmount: 36,
      },
    ])
    store.beginCheckout(101)
    store.previewResult = {
      goodsAmount: 36,
      deliveryFee: 3,
      discountAmount: 0,
      payAmount: 39,
      items: [
        {
          skuId: 1001,
          productName: 'Braised Chicken Rice',
          skuName: 'Large',
          quantity: 2,
          price: 18,
          totalAmount: 36,
        },
      ],
    }

    store.setCartItems([])

    expect(store.checkoutDraft).toBeNull()
    expect(store.previewResult).toBeNull()
  })

  it('fills receiver fields into the checkout draft from a selected address', () => {
    const store = useStudentCheckoutStore()

    store.setCartItems([
      {
        id: 1,
        storeId: 101,
        skuId: 1001,
        productName: 'Braised Chicken Rice',
        skuName: 'Large',
        price: 18,
        quantity: 2,
        totalAmount: 36,
      },
    ])
    store.beginCheckout(101)

    store.fillReceiver({
      contactName: 'Zhang San',
      contactPhone: '13800138000',
      fullAddress: 'South Campus Building 6 Room 302',
    })

    expect(store.checkoutDraft).toMatchObject({
      receiverName: 'Zhang San',
      receiverPhone: '13800138000',
      receiverAddress: 'South Campus Building 6 Room 302',
    })
  })

  it('stores the selected coupon id in the checkout draft', () => {
    const store = useStudentCheckoutStore()

    store.setCartItems([
      {
        id: 1,
        storeId: 101,
        skuId: 1001,
        productName: 'Braised Chicken Rice',
        skuName: 'Large',
        price: 18,
        quantity: 2,
        totalAmount: 36,
      },
    ])
    store.beginCheckout(101)

    store.setCoupon(9001)
    expect(store.checkoutDraft?.userCouponId).toBe(9001)

    store.setCoupon()
    expect(store.checkoutDraft?.userCouponId).toBeUndefined()
  })

  it('stores order remark text in the checkout draft', () => {
    const store = useStudentCheckoutStore()

    store.setCartItems([
      {
        id: 1,
        storeId: 101,
        skuId: 1001,
        productName: 'Braised Chicken Rice',
        skuName: 'Large',
        price: 18,
        quantity: 2,
        totalAmount: 36,
      },
    ])
    store.beginCheckout(101)

    store.setRemark('Less spicy please')

    expect(store.checkoutDraft?.remark).toBe('Less spicy please')
  })
})
