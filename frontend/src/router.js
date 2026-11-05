import { createRouter, createWebHistory } from 'vue-router'
import { session, isInstructor } from './store'
import { loadSession } from './api'

const routes = [
  { path: '/login', component: () => import('./views/Login.vue'), meta: { open: true } },
  { path: '/', component: () => import('./views/Home.vue') },
  { path: '/chapters/:id', component: () => import('./views/Chapter.vue') },
  { path: '/chapters/:chapterId/new', component: () => import('./views/QuestionForm.vue') },
  { path: '/questions/:id', component: () => import('./views/QuestionView.vue') },
  { path: '/questions/:id/edit', component: () => import('./views/QuestionForm.vue') },
  { path: '/mine', component: () => import('./views/Mine.vue') },
  { path: '/instructor/review', component: () => import('./views/instructor/Review.vue'), meta: { instructor: true } },
  { path: '/instructor/students', component: () => import('./views/instructor/Students.vue'), meta: { instructor: true } },
  { path: '/instructor/students/:id', component: () => import('./views/instructor/StudentDetail.vue'), meta: { instructor: true } },
  { path: '/instructor/chapters', component: () => import('./views/instructor/Chapters.vue'), meta: { instructor: true } },
  { path: '/instructor/flags', component: () => import('./views/instructor/Flags.vue'), meta: { instructor: true } },
  { path: '/instructor/exams', component: () => import('./views/instructor/Exams.vue'), meta: { instructor: true } },
  { path: '/instructor/exams/:id', component: () => import('./views/instructor/ExamDetail.vue'), meta: { instructor: true } },
  { path: '/instructor/exams/:id/print/:form', component: () => import('./views/instructor/ExamPrint.vue'), meta: { instructor: true, print: true } },
  { path: '/:rest(.*)*', redirect: '/' },
]

const router = createRouter({ history: createWebHistory(), routes, scrollBehavior: (to) => (to.hash ? { el: to.hash, top: 70 } : { top: 0 }) })

router.beforeEach(async (to) => {
  if (!session.loaded) await loadSession()
  if (!session.user && !to.meta.open) return '/login'
  if (to.meta.instructor && !isInstructor()) return '/'
})

export default router
