<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Button, Typography } from 'ant-design-vue'

const router = useRouter()

const start = () => {
  const roomId = Math.floor(Math.random() * 900000) + 100000
  router.push({ name: 'chat', params: { roomId } })
}
</script>

<template>
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center;">
    <div style="text-align: center;">
      <Typography.Title :level="2">AI 脑筋急转弯</Typography.Title>
      <Button type="primary" size="large" @click="start">开始游戏</Button>
    </div>
  </div>
</template>

<style scoped>
</style>
