import { jwtDecode } from '../utils/jwtDecode'

// Build a minimal JWT with a given payload
function buildJwt(payload: object): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const body = btoa(JSON.stringify(payload))
  return `${header}.${body}.fakesig`
}

describe('jwtDecode', () => {
  it('decodes a token with flat role field', () => {
    const token = buildJwt({
      sub: 'admin@test.com',
      id: 'abc-123',
      firstName: 'Admin',
      lastName: 'User',
      role: 'Administrator'
    })
    const user = jwtDecode(token)
    expect(user.email).toBe('admin@test.com')
    expect(user.role).toBe('Administrator')
    expect(user.firstName).toBe('Admin')
  })

  it('strips ROLE_ prefix', () => {
    const token = buildJwt({ sub: 'user@test.com', role: 'ROLE_Applicant' })
    const user = jwtDecode(token)
    expect(user.role).toBe('Applicant')
  })

  it('decodes roles array (Spring Security style)', () => {
    const token = buildJwt({ sub: 'user@test.com', roles: ['ROLE_Administrator'] })
    const user = jwtDecode(token)
    expect(user.role).toBe('Administrator')
  })

  it('throws on invalid JWT', () => {
    expect(() => jwtDecode('not.a.valid.jwt.token')).toThrow()
  })
})

