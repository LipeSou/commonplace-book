// Gera o ícone do app a partir do 円相 ensō — a marca do Commonplace Book.
// Traço de papel sobre fundo de tinta, o círculo sempre aberto.
// Roda com: npm run icon  (produz build/icon.ico e build/icon.png)
import sharp from 'sharp'
import pngToIco from 'png-to-ico'
import { writeFile } from 'node:fs/promises'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const here = dirname(fileURLToPath(import.meta.url))

// o mesmo traço do EnsoMark.vue, centrado numa moldura de tinta com cantos contidos
const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512">
  <rect width="512" height="512" rx="96" fill="#1b1a17"/>
  <g transform="translate(76,76) scale(3.6)">
    <path d="M 78 22 A 40 40 0 1 0 88 46" fill="none"
          stroke="#ebe3d0" stroke-width="7" stroke-linecap="round"/>
  </g>
</svg>`

const png = (size) => sharp(Buffer.from(svg)).resize(size, size).png().toBuffer()

// o .ico junta vários tamanhos; o Windows escolhe o certo pra cada lugar
const sizes = [16, 24, 32, 48, 64, 128, 256]
const layers = await Promise.all(sizes.map(png))

await writeFile(join(here, 'icon.ico'), await pngToIco(layers))
await writeFile(join(here, 'icon.png'), await png(512))

console.log('ícone gerado: build/icon.ico + build/icon.png')
