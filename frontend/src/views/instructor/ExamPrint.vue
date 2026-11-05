<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { get } from '../../api'

const route = useRoute()
const showKey = route.query.key === '1'
const letters = ['A', 'B', 'C', 'D']
const form = ref(null)
const error = ref('')
const print = () => window.print()

onMounted(async () => {
  try {
    form.value = await get(`/instructor/exams/${route.params.id}/forms/${route.params.form}`)
    document.title = `${form.value.title} - ${showKey ? 'Key' : 'Form'} ${form.value.form}`
  } catch (e) {
    error.value = e.message
  }
})
</script>

<template>
  <p v-if="error" class="error">{{ error }}</p>
  <template v-if="form">
    <p class="no-print row">
      <button class="primary" @click="print">Print or save as PDF</button>
      <RouterLink :to="`/instructor/exams/${route.params.id}`">Back to the exam</RouterLink>
    </p>

    <template v-if="showKey">
      <div class="exam-head">
        <h1 style="margin: 0">{{ form.title }} — Answer key, Form {{ form.form }}</h1>
        <div>Instructor copy. Do not give this page to students.</div>
      </div>
      <div class="key-grid">
        <div v-for="q in form.questions" :key="q.number"><b>{{ q.number }}.</b> {{ q.correct }} <span style="color: #777; font-size: 0.8em">(#{{ q.question_id }})</span></div>
      </div>
    </template>

    <template v-else>
      <div class="exam-head">
        <div class="row spread">
          <h1 style="margin: 0">{{ form.title }}</h1>
          <h1 style="margin: 0">Form {{ form.form }}</h1>
        </div>
        <div>SENG 403 Software Quality Assurance and Testing · Instructor: Gökçer Gökdal</div>
        <p>Name: <span class="line"></span> &nbsp; Student no: <span class="line" style="min-width: 120px"></span> &nbsp; Signature: <span class="line" style="min-width: 120px"></span></p>
        <div>{{ form.questions.length }} questions. Each question has one correct answer. Circle one letter.</div>
      </div>
      <div v-for="q in form.questions" :key="q.number" class="exam-q">
        <div class="pre"><b>{{ q.number }}.</b> {{ q.stem }}</div>
        <ol>
          <li v-for="(text, i) in q.options" :key="i">{{ letters[i] }}) {{ text }}</li>
        </ol>
      </div>
    </template>
  </template>
</template>
