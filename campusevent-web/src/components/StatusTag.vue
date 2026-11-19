<script setup>
import { computed } from 'vue'
import { statusText, statusType } from '@/utils/format'

const props = defineProps({
  status: { type: String, default: '' },
  size: { type: String, default: 'default' },
})

const label = computed(() => statusText(props.status))
const type = computed(() => statusType(props.status))
</script>

<template>
  <el-tag :type="type" :size="size" effect="light" round>{{ label }}</el-tag>
</template>
