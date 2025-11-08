import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../services/api'

function PresensiCheck({ userInfo, onLogout }) {
  const [jamMasuk, setJamMasuk] = useState('')
  const [jamKeluar, setJamKeluar] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [absensiForm, setAbsensiForm] = useState({
    tglAbsensi: '',
    kdStatus: ''
  })
  const [statusList, setStatusList] = useState([])

  useEffect(() => {
    loadStatusAbsen()
    loadTodayPresensi()
  }, [])

  const loadStatusAbsen = async () => {
    try {
      const now = new Date()
      const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
      const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0)
      const tglAwal = Math.floor(firstDay.getTime() / 1000)
      const tglAkhir = Math.floor(lastDay.getTime() / 1000)

      const response = await api.get('/presensi/combo/status-absen', {
        params: { tglAwal, tglAkhir }
      })
      setStatusList(response.data)
    } catch (err) {
      console.error('Error loading status absen:', err)
    }
  }

  const loadTodayPresensi = async () => {
    try {
      const now = new Date()
      const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      const todayEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59)
      const tglAwal = Math.floor(todayStart.getTime() / 1000)
      const tglAkhir = Math.floor(todayEnd.getTime() / 1000)

      const response = await api.get('/presensi/daftar/pegawai', {
        params: { tglAwal, tglAkhir }
      })
      
      if (response.data && response.data.length > 0) {
        const todayPresensi = response.data[0] // Ambil data pertama (hari ini)
        if (todayPresensi.jamMasuk) {
          setJamMasuk(todayPresensi.jamMasuk)
        }
        if (todayPresensi.jamKeluar) {
          setJamKeluar(todayPresensi.jamKeluar)
        }
      }
    } catch (err) {
      console.error('Error loading today presensi:', err)
    }
  }

  const handleCheckIn = async () => {
    setLoading(true)
    setError('')
    setMessage('')

    try {
      const response = await api.get('/presensi/in')
      if (response.data && response.data.jamMasuk) {
        setJamMasuk(response.data.jamMasuk)
        setMessage('Check-in berhasil!')
        // Refresh data presensi hari ini
        setTimeout(() => {
          loadTodayPresensi()
        }, 500)
      } else {
        setError('Response tidak valid dari server')
      }
    } catch (err) {
      const errorMessage = err.response?.data || 'Gagal melakukan check-in'
      setError(errorMessage)
      console.error('Check-in error:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleCheckOut = async () => {
    setLoading(true)
    setError('')
    setMessage('')

    try {
      const response = await api.get('/presensi/out')
      if (response.data && response.data.jamKeluar) {
        setJamKeluar(response.data.jamKeluar)
        setMessage('Check-out berhasil!')
        // Refresh data presensi hari ini
        setTimeout(() => {
          loadTodayPresensi()
        }, 500)
      } else {
        setError('Response tidak valid dari server')
      }
    } catch (err) {
      const errorMessage = err.response?.data || 'Gagal melakukan check-out'
      setError(errorMessage)
      console.error('Check-out error:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleAbsensi = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    setMessage('')

    if (!absensiForm.tglAbsensi || !absensiForm.kdStatus) {
      setError('Pilih tanggal dan status absensi')
      setLoading(false)
      return
    }

    try {
      const tglAbsensiEpoch = Math.floor(new Date(absensiForm.tglAbsensi).getTime() / 1000)
      const formData = new FormData()
      formData.append('tglAbsensi', tglAbsensiEpoch)
      formData.append('kdStatus', absensiForm.kdStatus)

      await api.post('/presensi/abseni', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })

      setMessage('Absensi berhasil direkam')
      setAbsensiForm({ tglAbsensi: '', kdStatus: '' })
    } catch (err) {
      setError(err.response?.data || 'Gagal merekam absensi')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <nav className="navbar">
        <h1>HRMIS - Presensi & Absensi</h1>
        <div>
          <Link to="/dashboard" style={{ color: 'white', marginRight: '15px', textDecoration: 'none' }}>Dashboard</Link>
          <button onClick={onLogout}>Logout</button>
        </div>
      </nav>
      <div className="container">
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
          {/* Check In/Out */}
          <div className="card">
            <h2>Check In / Check Out</h2>
            <div style={{ marginTop: '20px' }}>
              <div style={{ marginBottom: '20px' }}>
                <label style={{ display: 'block', marginBottom: '10px', fontWeight: 'bold' }}>
                  Jam Masuk Hari Ini
                </label>
                <div style={{ 
                  padding: '15px', 
                  background: '#f8f9fa', 
                  borderRadius: '4px',
                  fontSize: '18px',
                  fontWeight: 'bold',
                  textAlign: 'center'
                }}>
                  {jamMasuk || '-'}
                </div>
              </div>
              <div style={{ marginBottom: '20px' }}>
                <label style={{ display: 'block', marginBottom: '10px', fontWeight: 'bold' }}>
                  Jam Keluar Hari Ini
                </label>
                <div style={{ 
                  padding: '15px', 
                  background: '#f8f9fa', 
                  borderRadius: '4px',
                  fontSize: '18px',
                  fontWeight: 'bold',
                  textAlign: 'center'
                }}>
                  {jamKeluar || '-'}
                </div>
              </div>
              <div style={{ display: 'flex', gap: '10px' }}>
                <button 
                  className="btn btn-success" 
                  onClick={handleCheckIn}
                  disabled={loading}
                  style={{ flex: 1 }}
                >
                  {loading ? 'Loading...' : 'Check In'}
                </button>
                <button 
                  className="btn btn-danger" 
                  onClick={handleCheckOut}
                  disabled={loading}
                  style={{ flex: 1 }}
                >
                  {loading ? 'Loading...' : 'Check Out'}
                </button>
              </div>
              {message && <div className="success" style={{ marginTop: '10px' }}>{message}</div>}
              {error && <div className="error" style={{ marginTop: '10px' }}>{error}</div>}
            </div>
          </div>

          {/* Absensi Form */}
          <div className="card">
            <h2>Lapor Absensi / Izin</h2>
            <form onSubmit={handleAbsensi} style={{ marginTop: '20px' }}>
              <div className="form-group">
                <label>Tanggal Absensi *</label>
                <input
                  type="date"
                  value={absensiForm.tglAbsensi}
                  onChange={(e) => setAbsensiForm({ ...absensiForm, tglAbsensi: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Status Absensi *</label>
                <select
                  value={absensiForm.kdStatus}
                  onChange={(e) => setAbsensiForm({ ...absensiForm, kdStatus: e.target.value })}
                  required
                >
                  <option value="">Pilih Status</option>
                  {statusList.map(status => (
                    <option key={status.kdStatus} value={status.kdStatus}>
                      {status.namaStatus}
                    </option>
                  ))}
                </select>
              </div>
              <button 
                type="submit" 
                className="btn btn-primary" 
                disabled={loading}
                style={{ width: '100%' }}
              >
                {loading ? 'Loading...' : 'Simpan Absensi'}
              </button>
              {error && <div className="error" style={{ marginTop: '10px' }}>{error}</div>}
              {message && <div className="success" style={{ marginTop: '10px' }}>{message}</div>}
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}

export default PresensiCheck

