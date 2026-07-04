import { ref } from 'vue'

const KEY = 'commonplace-theme'
const theme = ref(localStorage.getItem(KEY) ?? 'ink')

function apply(value) {
  document.documentElement.dataset.theme = value
}

apply(theme.value)

export function useTheme() {
  function toggle() {
    theme.value = theme.value === 'ink' ? 'paper' : 'ink'
    localStorage.setItem(KEY, theme.value)
    apply(theme.value)
  }
  return { theme, toggle }
}
