<template>
  <main class="washi-grain">
    <h1>Commonplace Book</h1>
    <p>Toda prática começa no vazio.</p>
  </main>
</template>

<style scoped>
main {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--s4);
}

h1 {
  font-family: var(--f-display);
  font-weight: 600;
  font-size: var(--fs-display);
  margin: 0;
}

p {
  color: var(--text-muted);
  font-size: var(--fs-small);
  margin: 0;
}
</style>
