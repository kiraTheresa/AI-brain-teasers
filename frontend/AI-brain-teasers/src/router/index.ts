import { createRouter, createWebHistory } from 'vue-router'
import FirstPage from '../views/FirstPage.vue'
import ChatRoom from '../views/ChatRoom.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: FirstPage },
    { path: '/chat/:roomId?', name: 'chat', component: ChatRoom },
  ],
})

export default router
