<template>
  <el-footer class="app-footer">
    <span>Copyright &copy; {{ year }} ShortLink - 短链接管理系统</span>
  </el-footer>
</template>

<script setup>
const year = new Date().getFullYear()
</script>

<style scoped>
.app-footer {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: #999;
  border-top: 1px solid #e6e6e6;
  background: #fff;
}
</style>
