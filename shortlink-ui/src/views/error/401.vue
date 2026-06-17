<template>
  <div class="error-page">
    <h1>401</h1>
    <p>未授权 — 请先登录</p>
    <el-button type="primary" @click="$router.push('/login')">去登录</el-button>
  </div>
</template>

<style scoped>
.error-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.error-page h1 {
  font-size: 80px;
  color: #409EFF;
  margin: 0;
}

.error-page p {
  font-size: 18px;
  color: #666;
  margin: 20px 0;
}
</style>
