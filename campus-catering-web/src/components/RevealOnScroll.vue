<template>
  <div ref="rootRef" :class="['cc-fade-in', { 'is-visible': isVisible }]">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

const rootRef = ref<HTMLElement>()
const isVisible = ref(false)
let observer: IntersectionObserver | null = null

onMounted(() => {
  if (!rootRef.value) {
    return
  }

  observer = new IntersectionObserver(
    ([entry]) => {
      if (!entry) {
        return
      }
      if (entry.isIntersecting) {
        isVisible.value = true
        observer?.disconnect()
      }
    },
    { threshold: 0.15 },
  )

  observer.observe(rootRef.value)
})

onBeforeUnmount(() => {
  observer?.disconnect()
  observer = null
})
</script>
