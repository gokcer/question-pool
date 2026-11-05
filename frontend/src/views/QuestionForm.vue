<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post, put } from '../api'
import { isInstructor } from '../store'

const route = useRoute()
const router = useRouter()
const editId = route.params.id
const letters = ['a', 'b', 'c', 'd']
const form = reactive({
  chapter_id: Number(route.params.chapterId) || null,
  stem: '', option_a: '', option_b: '', option_c: '', option_d: '', correct: '',
  rationale_a: '', rationale_b: '', rationale_c: '', rationale_d: '', reference: '',
})
const chapter = ref(null)
const error = ref('')
const busy = ref(false)
const ready = ref(!editId)

onMounted(async () => {
  try {
    if (editId) {
      const q = await get(`/questions/${editId}`)
      for (const key of Object.keys(form)) form[key] = q[key] ?? ''
      ready.value = true
    }
    chapter.value = await get(`/chapters/${form.chapter_id}`)
  } catch (e) {
    error.value = e.message
  }
})

async function save() {
  busy.value = true
  error.value = ''
  try {
    let id = editId
    if (!editId) id = (await post('/questions', form)).id
    else if (isInstructor()) await put(`/instructor/questions/${editId}`, form)
    else await put(`/questions/${editId}`, form)
    router.push(`/questions/${id}`)
  } catch (e) {
    error.value = e.message
    busy.value = false
  }
}
</script>

<template>
  <p class="small"><RouterLink :to="editId ? `/questions/${editId}` : `/chapters/${form.chapter_id}?tab=questions`">← Back</RouterLink></p>
  <h1>{{ editId ? 'Change the question' : 'Write a question' }}</h1>
  <p v-if="chapter" class="muted">{{ chapter.title }}</p>

  <details class="card" open>
    <summary><b>What is a good question?</b></summary>
    <ul style="padding-left: 20px; margin: 8px 0">
      <li>It asks <b>one</b> thing from this chapter, in short and clear English.</li>
      <li>It makes the reader <b>think</b>: use a small example or situation. Do not only ask for a definition.</li>
      <li>Exactly <b>one</b> option is correct. The three wrong options look possible to someone who did not study.</li>
      <li>No "all of the above" and no "none of the above".</li>
      <li>You explain why each wrong option is wrong. This is the part where you learn the most.</li>
    </ul>
  </details>

  <form v-if="ready" @submit.prevent="save">
    <label for="stem">Question text</label>
    <textarea id="stem" v-model="form.stem" rows="4" maxlength="800" required></textarea>

    <h2>Options</h2>
    <p class="hint">Write four options, choose the correct one, and give a reason for every option.</p>
    <div v-for="l in letters" :key="l" class="card">
      <div class="row">
        <b>{{ l.toUpperCase() }}</b>
        <label class="inline"><input v-model="form.correct" type="radio" name="correct" :value="l.toUpperCase()" required /> This is the correct answer</label>
      </div>
      <input v-model="form['option_' + l]" type="text" maxlength="300" required :placeholder="'Option ' + l.toUpperCase()" style="margin-top: 8px" />
      <label :for="'why_' + l" class="small">{{ !form.correct ? 'Why is this correct or wrong?' : form.correct === l.toUpperCase() ? 'Why is this correct?' : 'Why is this wrong?' }}</label>
      <textarea :id="'why_' + l" v-model="form['rationale_' + l]" rows="2" maxlength="600" required></textarea>
    </div>

    <label for="reference">Where is this in the course? <span class="hint">For example: "Week 7, slide 12" or the section name in the chapter text</span></label>
    <input id="reference" v-model="form.reference" type="text" maxlength="200" required />

    <p v-if="error" class="error">{{ error }}</p>
    <p class="row">
      <button class="primary" :disabled="busy">{{ editId ? 'Save changes' : 'Send the question' }}</button>
      <span v-if="!isInstructor()" class="small muted">After you send it, an automatic check gives you feedback. Then the instructor approves it.</span>
    </p>
  </form>
  <p v-else-if="error" class="error">{{ error }}</p>
</template>
