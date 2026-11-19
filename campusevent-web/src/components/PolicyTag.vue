<script setup>
import { computed } from 'vue'
import { policyText, policyType } from '@/utils/format'

const props = defineProps({
  type: { type: String, default: '' },
  size: { type: String, default: 'default' },
})

const label = computed(() => policyText(props.type))
const tagType = computed(() => policyType(props.type))
</script>

<template>
  <el-tag :type="tagType" :size="size" effect="plain" round>{{ label }}</el-tag>
</template>
