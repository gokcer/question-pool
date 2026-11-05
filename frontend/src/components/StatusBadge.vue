<script setup>
defineProps({ status: String })
const labels = {
  PENDING: ['Waiting for approval', 'amber'],
  NEEDS_REVISION: ['Needs changes', 'amber'],
  APPROVED: ['Approved', 'green'],
  REJECTED: ['Rejected', 'red'],
}
</script>

<template>
  <span :class="['badge', labels[status]?.[1]]">{{ labels[status]?.[0] || status }}</span>
</template>
