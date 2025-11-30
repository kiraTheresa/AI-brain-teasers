<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getRooms, postChat } from '@/api'
import { message as antdMessage } from 'ant-design-vue'

type ChatMsg = { role: 'SYSTEM' | 'USER' | 'ASSISTANT'; content: string }

const route = useRoute()
const roomId = ref<number>(Number(route.params.roomId) || Math.floor(Math.random() * 900000) + 100000)
const input = ref('')
const starting = ref(false)
const started = ref(false)
const ended = ref(false)
const rooms = ref<Array<{ roomId: number; chatMessageList: ChatMsg[] }>>([])
const messages = reactive<ChatMsg[]>([])

const fetchRooms = async () => {
  try {
    rooms.value = await getRooms()
  } catch (e) {}
}

const startGame = async () => {
  if (started.value || starting.value) return
  starting.value = true
  try {
    const reply = await postChat(roomId.value, '开始')
    messages.push({ role: 'USER', content: '开始' })
    messages.push({ role: 'ASSISTANT', content: reply })
    started.value = true
    if (reply.includes('游戏结束')) ended.value = true
  } catch (e) {
    antdMessage.error('启动失败')
  } finally {
    starting.value = false
    fetchRooms()
  }
}

const send = async () => {
  const content = input.value.trim()
  if (!content) return
  try {
    const reply = await postChat(roomId.value, content)
    messages.push({ role: 'USER', content })
    messages.push({ role: 'ASSISTANT', content: reply })
    input.value = ''
    if (reply.includes('游戏结束')) ended.value = true
  } catch (e) {
    antdMessage.error('发送失败')
  } finally {
    fetchRooms()
  }
}

onMounted(() => {
  fetchRooms()
})

watch(
  () => route.params.roomId,
  (val) => {
    if (val) {
      roomId.value = Number(val)
      messages.splice(0, messages.length)
      started.value = false
      ended.value = false
    }
  }
)
</script>

<template>
  <div style="display: grid; grid-template-columns: 240px 1fr; min-height: 100vh;">
    <div style="border-right: 1px solid #eee; padding: 16px;">
      <div style="margin-bottom: 12px; font-weight: 600;">历史对话</div>
      <div v-for="r in rooms" :key="r.roomId" style="margin-bottom: 8px;">
        <a-button block @click="$router.push({ name: 'chat', params: { roomId: r.roomId } })">对话 {{ r.roomId }}</a-button>
      </div>
    </div>
    <div style="padding: 16px;">
      <div style="margin-bottom: 12px; font-weight: 600;">AI 脑筋急转弯 · 房间 {{ roomId }}</div>
      <div style="display: flex; gap: 8px; margin-bottom: 12px;">
        <a-button type="primary" :disabled="started" :loading="starting" @click="startGame">开始</a-button>
        <a-button danger :disabled="ended">结束</a-button>
      </div>

      <div style="border: 1px solid #eee; border-radius: 8px; padding: 12px; min-height: 320px;">
        <div v-for="(m, i) in messages" :key="i" :style="{ display: 'flex', justifyContent: m.role==='USER' ? 'flex-end' : 'flex-start', marginBottom: '10px' }">
          <div style="display:flex; align-items:center; gap:8px;">
            <a-avatar v-if="m.role!=='USER'">AI</a-avatar>
            <div :style="{maxWidth:'60%', padding:'8px 12px', border:'1px solid #d9d9d9', borderRadius:'12px', background:m.role==='USER' ? '#e6f4ff' : '#fff' }">{{ m.content }}</div>
            <a-avatar v-if="m.role==='USER'">我</a-avatar>
          </div>
        </div>
      </div>

      <div style="display: flex; gap: 8px; margin-top: 12px;">
        <a-input v-model:value="input" placeholder="请输入内容" @keyup.enter="send" />
        <a-button type="primary" @click="send">发送</a-button>
      </div>
    </div>
  </div>
  </template>

<style scoped>
</style>
