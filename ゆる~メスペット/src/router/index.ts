import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
// import PlusFile from '@/views/PlusFile.vue'

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: { name: 'memo-list' } },
  {
    path: '/memos',
    name: 'memo-list',
    component: () => import('@/views/Home.vue') // リスト画面
  },
  {
    path: '/memos/add',
    alias: ['/memo/add'],
    name: 'memo-add',
    component: () => import('../views/Form.vue')
  },
  // Single Form Route : id is dosen't matter
  {
    path: '/memos/:id(\\d+)',
    name: 'memo-form',
    component: () => import('@/views/Form.vue'),
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default createRouter({
  history: createWebHistory(),
  routes
})
//export default router
