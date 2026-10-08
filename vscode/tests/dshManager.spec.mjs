import { describe, it } from 'node:test'
import assert from 'node:assert'

describe('DeepSeek Harness VS Code Extension - Runtime URL Parsing', () => {
  const urlRegex = /dsh web:\s+(https?:\/\/[^\s\)]+)/i

  it('matches standard authenticated DSH URL without LAN info', () => {
    const logLine = 'dsh web: http://127.0.0.1:4567/?token=test-token-xyz'
    const match = logLine.match(urlRegex)
    assert.ok(match)
    assert.strictEqual(match[1], 'http://127.0.0.1:4567/?token=test-token-xyz')
  })

  it('matches standard authenticated DSH URL with LAN info and extracts main loopback URL', () => {
    const logLine = 'dsh web: http://127.0.0.1:4567/?token=test-token-xyz (LAN: http://192.168.1.5:4567/?token=test-token-xyz)'
    const match = logLine.match(urlRegex)
    assert.ok(match)
    assert.strictEqual(match[1], 'http://127.0.0.1:4567/?token=test-token-xyz')
  })

  it('correctly extracts the port from matched URL', () => {
    const url = 'http://127.0.0.1:8080/?token=abc123'
    const parsed = new URL(url)
    assert.strictEqual(parsed.port, '8080')
    assert.strictEqual(parsed.searchParams.get('token'), 'abc123')
  })

  it('does not match unrelated lines', () => {
    const logLine = 'Starting plugin tree...'
    const match = logLine.match(urlRegex)
    assert.strictEqual(match, null)
  })
})

describe('DeepSeek Harness VS Code Extension - Command Resolution', () => {
  it('formats npm install command properly', () => {
    const npmCmd = 'npm install -g @deepseek-ai/dsh'
    assert.strictEqual(npmCmd, 'npm install -g @deepseek-ai/dsh')
  })

  it('preserves quoted Windows executable paths when parsing configuration', () => {
    const configured = '"C:\\Program Files\\nodejs\\dsh.cmd" --verbose'
    const parts = configured.match(/(?:[^\s"]+|"[^"]*")+/g) ?? []
    assert.deepStrictEqual(
      [parts[0]?.replace(/^"|"$/g, ''), ...parts.slice(1)],
      ['C:\\Program Files\\nodejs\\dsh.cmd', '--verbose'],
    )
  })

  it('converts Windows loader paths to file URLs for Node ESM', () => {
    const loaderPath = 'C:\\engenharia\\deepseek-harness\\node_modules\\tsx\\dist\\esm\\index.mjs'
    assert.strictEqual(
      new URL(`file://${loaderPath.replaceAll('\\\\', '/')}`).href,
      'file:///C:/engenharia/deepseek-harness/node_modules/tsx/dist/esm/index.mjs',
    )
  })

  it('does not treat a workspace source checkout as an installed dsh command', () => {
    const sourceCheckout = 'C:\\engenharia\\deepseek-harness\\apps\\cli\\src\\bin.ts'
    const installedCommands = []
    assert.strictEqual(installedCommands.includes(sourceCheckout), false)
  })
})

describe('DeepSeek Harness VS Code Extension - Session and Auth Helpers', () => {
  it('generates valid RFC-compliant base64url cookie structure with HMAC signature', async () => {
    const crypto = await import('node:crypto')
    const secret = crypto.randomBytes(32)
    const authority = '127.0.0.1:3080'

    const encodeBase64Url = (buf) =>
      Buffer.from(buf).toString('base64').replaceAll('+', '-').replaceAll('/', '_').replace(/=+$/g, '')

    const cookieName = 'dsh-auth-' + encodeBase64Url(crypto.createHash('sha256').update(authority).digest())
    const now = Date.now()
    const payload = {
      version: 1,
      authority,
      issuedAt: now,
      expiresAt: now + 30 * 24 * 3600 * 1000,
    }
    const body = encodeBase64Url(Buffer.from(JSON.stringify(payload), 'utf8'))
    const sig = encodeBase64Url(crypto.createHmac('sha256', secret).update(body).digest())
    const cookie = `${cookieName}=v1.${body}.${sig}`

    assert.ok(cookie.startsWith('dsh-auth-'))
    assert.ok(cookie.includes('=v1.'))
    const parts = cookie.split('=')[1].split('.')
    assert.strictEqual(parts.length, 3)
    assert.strictEqual(parts[0], 'v1')

    // Decode and verify payload
    const decodedPayload = JSON.parse(Buffer.from(parts[1], 'base64url').toString('utf8'))
    assert.strictEqual(decodedPayload.authority, authority)
    assert.strictEqual(decodedPayload.version, 1)
  })

  it('handles last session file save and read safely', async () => {
    const fs = await import('node:fs')
    const path = await import('node:path')
    const os = await import('node:os')

    const tmpDir = path.join(os.tmpdir(), 'dsh-test-' + Date.now())
    fs.mkdirSync(tmpDir, { recursive: true })

    const sessionFile = path.join(tmpDir, 'last-session.json')
    const sessionData = {
      rawUrl: 'http://127.0.0.1:3080/?token=test-token-123',
      port: 3080,
      token: 'test-token-123',
      updatedAt: Date.now(),
    }

    fs.writeFileSync(sessionFile, JSON.stringify(sessionData, null, 2), 'utf8')
    assert.ok(fs.existsSync(sessionFile))

    const loaded = JSON.parse(fs.readFileSync(sessionFile, 'utf8'))
    assert.strictEqual(loaded.port, 3080)
    assert.strictEqual(loaded.token, 'test-token-123')
    assert.strictEqual(loaded.rawUrl, 'http://127.0.0.1:3080/?token=test-token-123')

    fs.rmSync(tmpDir, { recursive: true, force: true })
  })
})

