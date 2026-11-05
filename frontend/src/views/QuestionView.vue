<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post, del } from '../api'
import { isInstructor } from '../store'
import StatusBadge from '../components/StatusBadge.vue'
import AiReview from '../components/AiReview.vue'
import Thread from '../components/Thread.vue'

const id = useRoute().params.id
const router = useRouter()
const letters = ['A', 'B', 'C', 'D']
const q = ref(null)
const selected = ref('')
const error = ref('')
const busy = ref(false)
const nextId = ref(null)
const flagged = ref(false)
let timer = null

async function load() {
  q.value = await get(`/questions/${id}`)
  // While the automatic check runs, look again every few seconds.
  clearTimeout(timer)
  if (q.value.ai_status === 'RUNNING') timer = setTimeout(() => load().catch(() => {}), 4000)
}

async function findNext() {
  const list = await get(`/chapters/${q.value.chapter_id}/questions`)
  const todo = list.filter((x) => x.status === 'APPROVED' && !x.mine && !x.attempted && x.id !== q.value.id)
  nextId.value = (todo.find((x) => x.id > q.value.id) || todo[0])?.id ?? null
}

onMounted(async () => {
  try {
    await load()
    if (!isInstructor()) await findNext()
  } catch (e) {
    error.value = e.message
  }
})
onUnmounted(() => clearTimeout(timer))

async function answer() {
  busy.value = true
  error.value = ''
  try {
    q.value = await post(`/questions/${id}/attempt`, { selected: selected.value })
  } catch (e) {
    error.value = e.message
  }
  busy.value = false
}

async function remove() {
  if (!confirm('Delete this question? You cannot undo this.')) return
  try {
    await del(`/questions/${id}`)
    router.push(`/chapters/${q.value.chapter_id}?tab=questions`)
  } catch (e) {
    error.value = e.message
  }
}

async function flag() {
  const reason = prompt('What is wrong with this question? (for example: the marked answer is wrong, two options are correct)')
  if (!reason) return
  try {
    await post(`/questions/${id}/flag`, { reason })
    flagged.value = true
  } catch (e) {
    error.value = e.message
  }
}

const editable = computed(() => isInstructor() || (q.value?.mine && ['PENDING', 'NEEDS_REVISION'].includes(q.value.status)))
const discussable = computed(() => q.value?.revealed && (q.value.status === 'APPROVED' || isInstructor()))
function optionClass(l) {
  if (!q.value.revealed) return { picked: selected.value === l }
  return { correct: q.value.correct === l, wrong: q.value.my_selected === l && q.value.correct !== l }
}
</script>

<template>
  <p v-if="error" class="error">{{ error }}</p>
  <template v-if="q">
    <p class="small"><RouterLink :to="`/chapters/${q.chapter_id}?tab=questions`">← Questions of this chapter</RouterLink></p>
    <div class="row small muted">
      <span>#{{ q.id }}</span>
      <span v-if="q.mine" class="badge blue">Mine</span>
      <span v-else>by {{ q.author }}</span>
      <StatusBadge v-if="q.mine || isInstructor()" :status="q.status" />
    </div>
    <h1 class="pre">{{ q.stem }}</h1>

    <!-- Author: feedback and next steps -->
    <template v-if="q.mine">
      <div v-if="q.status === 'PENDING'" class="note">Your question waits for the instructor. You can still change it.</div>
      <div v-if="q.status === 'NEEDS_REVISION'" class="note amber"><b>The instructor asks for changes:</b> {{ q.instructor_note }}</div>
      <div v-if="q.status === 'REJECTED'" class="note red"><b>Rejected:</b> {{ q.instructor_note }} You can write a new question for this chapter.</div>
      <div v-if="q.status === 'APPROVED'" class="note green">Approved. Other students can answer it now. Follow the discussion below.</div>
      <AiReview v-if="q.status !== 'APPROVED'" :status="q.ai_status" :review="q.ai_review" />
    </template>
    <AiReview v-else-if="isInstructor()" :status="q.ai_status" :review="q.ai_review" />

    <!-- Before the answer: choose one option -->
    <template v-if="!q.revealed">
      <label v-for="l in letters" :key="l" class="option" :class="optionClass(l)">
        <input v-model="selected" type="radio" name="answer" :value="l" />
        <span class="letter">{{ l }}</span>
        <span class="grow">{{ q['option_' + l.toLowerCase()] }}</span>
      </label>
      <p class="row">
        <button class="primary" :disabled="!selected || busy" @click="answer">Answer</button>
        <span class="small muted">You can answer only one time. After that you see the correct answer and the discussion.</span>
      </p>
    </template>

    <!-- After the answer: key, reasons, explanation -->
    <template v-else>
      <div v-if="q.my_selected" :class="['note', q.my_selected === q.correct ? 'green' : 'red']">
        <b>{{ q.my_selected === q.correct ? 'Correct!' : 'Not correct.' }}</b>
        You chose {{ q.my_selected }}. The correct answer is {{ q.correct }}.
      </div>
      <div v-for="l in letters" :key="l" class="option" :class="optionClass(l)">
        <span class="letter">{{ l }}</span>
        <div class="grow">
          <div>
            {{ q['option_' + l.toLowerCase()] }}
            <span v-if="q.correct === l" class="badge green">correct</span>
            <span v-if="q.my_selected === l" class="badge">your answer</span>
          </div>
          <div class="why">{{ q['rationale_' + l.toLowerCase()] }}</div>
        </div>
      </div>
      <p class="small muted">The reasons above are written by the student who wrote the question. Course reference: {{ q.reference }}</p>
      <div v-if="q.explanation" class="note">
        <b>Explanation</b> <span class="small muted">(written by AI from the chapter text)</span>
        <div class="pre">{{ q.explanation }}</div>
      </div>
    </template>

    <div class="row" style="margin: 14px 0">
      <RouterLink v-if="q.revealed && nextId" :to="`/questions/${nextId}`" class="btn primary">Next question →</RouterLink>
      <RouterLink v-if="editable" :to="`/questions/${q.id}/edit`" class="btn">Change</RouterLink>
      <button v-if="q.mine && q.status !== 'APPROVED'" class="danger" @click="remove">Delete</button>
      <button v-if="q.revealed && !q.mine && !isInstructor() && !flagged" @click="flag">Report a problem</button>
      <span v-if="flagged" class="small muted">Thank you. The instructor will look at it.</span>
    </div>

    <Thread v-if="discussable" :question-id="q.id" />
  </template>
</template>
