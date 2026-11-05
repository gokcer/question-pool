<script setup>
import { ref, onMounted } from 'vue'
import { get } from '../api'
import { session, isInstructor } from '../store'

const chapters = ref(null)
const error = ref('')
const target = session.user.questions_per_chapter

onMounted(async () => {
  try {
    chapters.value = await get('/chapters')
  } catch (e) {
    error.value = e.message
  }
})
</script>

<template>
  <h1>Chapters</h1>
  <div v-if="!isInstructor()" class="note">
    <b>How it works:</b> read the chapter, write <b>{{ target }} questions</b> for each chapter, then answer the
    questions of other students and talk about them. The midterm questions come from this pool.
    <div class="small muted">Other students see your nickname <b>{{ session.user.nickname }}</b>, not your real name.</div>
  </div>
  <p v-if="error" class="error">{{ error }}</p>
  <p v-if="!chapters && !error" class="muted">Loading…</p>

  <RouterLink v-for="c in chapters" :key="c.id" :to="`/chapters/${c.id}`" class="card">
    <div class="row spread">
      <b>{{ c.title }}</b>
      <span v-if="!c.open" class="badge">Closed for new questions</span>
    </div>
    <div class="row small muted" style="margin-top: 6px">
      <template v-if="!isInstructor()">
        <span :class="['badge', c.my_questions >= target ? 'green' : 'amber']">My questions {{ c.my_questions }}/{{ target }}</span>
        <span>Answered {{ c.my_attempts }} of {{ c.approved }}</span>
        <span v-if="c.my_attempts">Correct {{ c.my_correct }}</span>
      </template>
      <span v-else>{{ c.approved }} approved questions</span>
      <span v-if="!c.has_doc_en && !c.has_doc_tr">No chapter text yet</span>
    </div>
    <div v-if="!isInstructor() && c.approved" class="bar"><i :style="{ width: (100 * c.my_attempts) / c.approved + '%' }"></i></div>
  </RouterLink>
</template>
