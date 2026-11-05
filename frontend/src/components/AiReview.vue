<script setup>
import { computed } from 'vue'

const props = defineProps({ status: String, review: String })
const r = computed(() => {
  try {
    return props.review ? JSON.parse(props.review) : null
  } catch {
    return null
  }
})
const list = (v) => (Array.isArray(v) ? v : [])
</script>

<template>
  <div v-if="status === 'RUNNING'" class="note">The automatic check is running. It takes about half a minute…</div>
  <div v-else-if="status === 'FAILED'" class="note amber">The automatic check did not work this time. The instructor still sees your question.</div>
  <div v-else-if="status === 'DONE' && r" :class="['note', r.verdict === 'OK' ? 'green' : 'amber']">
    <b>Automatic check: {{ r.verdict === 'OK' ? 'looks good' : 'please improve' }}</b>
    <span class="small muted"> (made by AI, it can be wrong; the instructor decides)</span>
    <div v-if="r.key_correct === 'no'" class="error">The marked answer may be wrong. Check your key.</div>
    <div v-else-if="r.key_correct === 'unsure'" class="small">The check is not sure that the marked answer is correct.</div>
    <ul v-if="list(r.issues).length" style="margin: 6px 0; padding-left: 20px">
      <li v-for="(issue, i) in list(r.issues)" :key="i">{{ issue }}</li>
    </ul>
    <div v-if="list(r.duplicate_of).length" class="small">
      Maybe the same as question <b>{{ list(r.duplicate_of).map((d) => '#' + d).join(', ') }}</b>. Find it in the chapter list and compare.
    </div>
    <div v-if="r.english_suggestion" class="small"><b>Simpler English for the question text:</b> {{ r.english_suggestion }}</div>
  </div>
</template>
