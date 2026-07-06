<template>
  <section v-if="tags.length" class="tag-panel">
    <p class="label">Tags</p>
    <div class="stamps">
      <TagStamp
        v-for="tag in tags"
        :key="tag.name"
        :name="tag.name"
        :count="tag.count"
        :muted="tag.name !== active"
        @pick="$emit('pick', tag.name)"
      />
    </div>
  </section>
</template>

<script setup>
import TagStamp from './TagStamp.vue'

defineProps({
  tags: { type: Array, default: () => [] },
  active: { type: String, default: '' }
})

defineEmits(['pick'])
</script>

<style scoped>
.tag-panel {
  display: flex;
  flex-direction: column;
  gap: var(--s3);
}

.label {
  font-family: var(--f-body);
  font-size: var(--fs-small);
  color: var(--text-muted);
  margin: 0;
}

.stamps {
  display: flex;
  flex-wrap: wrap;
  gap: var(--s2);
}
</style>
