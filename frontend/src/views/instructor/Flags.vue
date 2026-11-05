<script setup>
import { ref, onMounted } from 'vue'
import { get, post, when } from '../../api'

const flags = ref([])
const error = ref('')
async function load() {
  flags.value = await get('/instructor/flags')
}
onMounted(() => load().catch((e) => (error.value = e.message)))
async function resolve(f) {
  await post(`/instructor/flags/${f.id}/resolve`)
  await load()
}
</script>

<template>
  <h1>Problems reported by students</h1>
  <p v-if="error" class="error">{{ error }}</p>
  <p v-if="!flags.length" class="muted">No reports.</p>
  <div v-for="f in flags" :key="f.id" class="card">
    <div class="row small muted">
      <span :class="['badge', f.resolved ? 'green' : 'red']">{{ f.resolved ? 'Done' : 'Open' }}</span>
      <span>{{ f.student_name }} ({{ f.nickname }})</span>
      <span>{{ when(f.created_at) }}</span>
    </div>
    <p class="pre"><b>{{ f.reason }}</b></p>
    <p class="small">On <RouterLink :to="`/questions/${f.question_id}`">#{{ f.question_id }}</RouterLink>: {{ f.stem }}</p>
    <div class="row">
      <RouterLink :to="`/questions/${f.question_id}/edit`" class="btn">Edit the question</RouterLink>
      <button v-if="!f.resolved" class="primary" @click="resolve(f)">Mark as done</button>
    </div>
  </div>
</template>
