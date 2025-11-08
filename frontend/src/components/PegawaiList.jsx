import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../services/api'

function PegawaiList({ userInfo, onLogout }) {
  const [pegawaiList, setPegawaiList] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [editingPegawai, setEditingPegawai] = useState(null)
  const [formData, setFormData] = useState({
    namaLengkap: '',
    email: '',
    tempatLahir: '',
    tanggalLahir: '',
    kdJenisKelamin: '',
    kdPendidikan: '',
    kdJabatan: '',
    kdDepartemen: '',
    kdUnitKerja: '',
    password: '',
    passwordC: ''
  })
  const [comboData, setComboData] = useState({
    jabatan: [],
    departemen: [],
    unitKerja: [],
    pendidikan: [],
    jenisKelamin: []
  })
  const [showPhotoForm, setShowPhotoForm] = useState(false)
  const [selectedPegawai, setSelectedPegawai] = useState(null)
  const [photoFile, setPhotoFile] = useState(null)
  const [photoFileName, setPhotoFileName] = useState('')
  const [photoError, setPhotoError] = useState('')
  const [photoLoading, setPhotoLoading] = useState(false)

  useEffect(() => {
    if (userInfo?.profile === 'Admin' || userInfo?.namaDepartemen === 'HRD') {
      loadPegawai()
      loadComboData()
    }
  }, [userInfo])

  const loadPegawai = async () => {
    try {
      const response = await api.get('/pegawai/daftar')
      setPegawaiList(response.data)
    } catch (err) {
      setError(err.response?.data || 'Gagal memuat data pegawai')
    } finally {
      setLoading(false)
    }
  }

  const loadComboData = async () => {
    try {
      const [jabatan, departemen, unitKerja, pendidikan, jenisKelamin] = await Promise.all([
        api.get('/pegawai/combo/jabatan'),
        api.get('/pegawai/combo/departemen'),
        api.get('/pegawai/combo/unit-kerja'),
        api.get('/pegawai/combo/pendidikan'),
        api.get('/pegawai/combo/jenis-kelamin')
      ])
      setComboData({
        jabatan: jabatan.data,
        departemen: departemen.data,
        unitKerja: unitKerja.data,
        pendidikan: pendidikan.data,
        jenisKelamin: jenisKelamin.data
      })
    } catch (err) {
      console.error('Error loading combo data:', err)
    }
  }

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    const data = {
      ...formData,
      tanggalLahir: formData.tanggalLahir ? Math.floor(new Date(formData.tanggalLahir).getTime() / 1000) : null
    }

    try {
      const formDataToSend = new FormData()
      Object.keys(data).forEach(key => {
        if (data[key] !== null && data[key] !== '') {
          formDataToSend.append(key, data[key])
        }
      })

      if (editingPegawai) {
        formDataToSend.append('idUser', editingPegawai.idUser)
        await api.post('/pegawai/admin-ubah-pegawai', formDataToSend, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
      } else {
        await api.post('/pegawai/admin-tambah-pegawai', formDataToSend, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
      }

      setShowForm(false)
      setEditingPegawai(null)
      setFormData({
        namaLengkap: '', email: '', tempatLahir: '', tanggalLahir: '',
        kdJenisKelamin: '', kdPendidikan: '', kdJabatan: '', kdDepartemen: '',
        kdUnitKerja: '', password: '', passwordC: ''
      })
      loadPegawai()
    } catch (err) {
      setError(err.response?.data || 'Gagal menyimpan data')
    }
  }

  const handleEdit = (pegawai) => {
    setEditingPegawai(pegawai)
    const tanggalLahir = pegawai.tanggalLahir ? new Date(pegawai.tanggalLahir * 1000).toISOString().split('T')[0] : ''
    setFormData({
      namaLengkap: pegawai.namaLengkap || '',
      email: pegawai.email || '',
      tempatLahir: pegawai.tempatLahir || '',
      tanggalLahir: tanggalLahir,
      kdJenisKelamin: pegawai.kdJenisKelamin || '',
      kdPendidikan: pegawai.kdPendidikan || '',
      kdJabatan: pegawai.kdJabatan || '',
      kdDepartemen: pegawai.kdDepartemen || '',
      kdUnitKerja: pegawai.kdUnitKerja || '',
      password: '',
      passwordC: ''
    })
    setShowForm(true)
  }

  const formatEpoch = (epoch) => {
    if (!epoch) return '-'
    const date = new Date(epoch * 1000)
    return date.toLocaleDateString('id-ID')
  }

  const handleUbahPhoto = (pegawai) => {
    setSelectedPegawai(pegawai)
    setShowPhotoForm(true)
    setPhotoFile(null)
    setPhotoFileName('')
    setPhotoError('')
  }

  const handlePhotoChange = (e) => {
    const file = e.target.files[0]
    if (file) {
      setPhotoFile(file)
      if (!photoFileName) {
        setPhotoFileName(file.name)
      }
      setPhotoError('')
    }
  }

  const handlePhotoSubmit = async (e) => {
    e.preventDefault()
    if (!photoFile || !photoFileName) {
      setPhotoError('Pilih file foto dan isi nama file')
      return
    }

    try {
      setPhotoLoading(true)
      setPhotoError('')
      const formData = new FormData()
      formData.append('idUser', selectedPegawai.idUser)
      formData.append('namaFile', photoFileName)
      formData.append('files', photoFile)

      await api.post('/pegawai/admin-ubah-photo', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })

      setShowPhotoForm(false)
      setSelectedPegawai(null)
      setPhotoFile(null)
      setPhotoFileName('')
      loadPegawai()
      alert('Foto pegawai berhasil diubah')
    } catch (err) {
      setPhotoError(err.response?.data || 'Gagal mengubah foto')
    } finally {
      setPhotoLoading(false)
    }
  }

  if (userInfo?.profile !== 'Admin' && userInfo?.namaDepartemen !== 'HRD') {
    return (
      <div>
        <nav className="navbar">
          <h1>HRMIS - Pegawai</h1>
          <div>
            <Link to="/dashboard" style={{ color: 'white', marginRight: '15px', textDecoration: 'none' }}>Dashboard</Link>
            <button onClick={onLogout}>Logout</button>
          </div>
        </nav>
        <div className="container">
          <div className="card">
            <h2>Akses Ditolak</h2>
            <p>Hanya Admin atau pegawai HRD yang dapat mengakses halaman ini.</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div>
      <nav className="navbar">
        <h1>HRMIS - Manajemen Pegawai</h1>
        <div>
          <Link to="/dashboard" style={{ color: 'white', marginRight: '15px', textDecoration: 'none' }}>Dashboard</Link>
          <button onClick={onLogout}>Logout</button>
        </div>
      </nav>
      <div className="container">
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2>Daftar Pegawai</h2>
            <button className="btn btn-primary" onClick={() => {
              setShowForm(true)
              setEditingPegawai(null)
              setFormData({
                namaLengkap: '', email: '', tempatLahir: '', tanggalLahir: '',
                kdJenisKelamin: '', kdPendidikan: '', kdJabatan: '', kdDepartemen: '',
                kdUnitKerja: '', password: '', passwordC: ''
              })
            }}>
              Tambah Pegawai
            </button>
          </div>

          {error && <div className="error">{error}</div>}

          {showForm && (
            <div className="card" style={{ marginTop: '20px', background: '#f8f9fa' }}>
              <h3>{editingPegawai ? 'Edit Pegawai' : 'Tambah Pegawai Baru'}</h3>
              <form onSubmit={handleSubmit}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                  <div className="form-group">
                    <label>Nama Lengkap *</label>
                    <input type="text" name="namaLengkap" value={formData.namaLengkap} onChange={handleChange} required />
                  </div>
                  <div className="form-group">
                    <label>Email *</label>
                    <input type="email" name="email" value={formData.email} onChange={handleChange} required />
                  </div>
                  <div className="form-group">
                    <label>Tempat Lahir</label>
                    <input type="text" name="tempatLahir" value={formData.tempatLahir} onChange={handleChange} />
                  </div>
                  <div className="form-group">
                    <label>Tanggal Lahir</label>
                    <input type="date" name="tanggalLahir" value={formData.tanggalLahir} onChange={handleChange} />
                  </div>
                  <div className="form-group">
                    <label>Jenis Kelamin</label>
                    <select name="kdJenisKelamin" value={formData.kdJenisKelamin} onChange={handleChange}>
                      <option value="">Pilih</option>
                      {comboData.jenisKelamin.map(jk => (
                        <option key={jk.kdJenisKelamin} value={jk.kdJenisKelamin}>{jk.namaJenisKelamin}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Pendidikan</label>
                    <select name="kdPendidikan" value={formData.kdPendidikan} onChange={handleChange}>
                      <option value="">Pilih</option>
                      {comboData.pendidikan.map(p => (
                        <option key={p.kdPendidikan} value={p.kdPendidikan}>{p.namaPendidikan}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Jabatan</label>
                    <select name="kdJabatan" value={formData.kdJabatan} onChange={handleChange}>
                      <option value="">Pilih</option>
                      {comboData.jabatan.map(j => (
                        <option key={j.kdJabatan} value={j.kdJabatan}>{j.namaJabatan}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Departemen</label>
                    <select name="kdDepartemen" value={formData.kdDepartemen} onChange={handleChange}>
                      <option value="">Pilih</option>
                      {comboData.departemen.map(d => (
                        <option key={d.kdDepartemen} value={d.kdDepartemen}>{d.namaDepartemen}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Unit Kerja</label>
                    <select name="kdUnitKerja" value={formData.kdUnitKerja} onChange={handleChange}>
                      <option value="">Pilih</option>
                      {comboData.unitKerja.map(u => (
                        <option key={u.kdUnitKerja} value={u.kdUnitKerja}>{u.namaUnitKerja}</option>
                      ))}
                    </select>
                  </div>
                  {!editingPegawai && (
                    <>
                      <div className="form-group">
                        <label>Password *</label>
                        <input type="password" name="password" value={formData.password} onChange={handleChange} required />
                      </div>
                      <div className="form-group">
                        <label>Konfirmasi Password *</label>
                        <input type="password" name="passwordC" value={formData.passwordC} onChange={handleChange} required />
                      </div>
                    </>
                  )}
                  {editingPegawai && (
                    <>
                      <div className="form-group">
                        <label>Password Baru (kosongkan jika tidak diubah)</label>
                        <input type="password" name="password" value={formData.password} onChange={handleChange} />
                      </div>
                      <div className="form-group">
                        <label>Konfirmasi Password Baru</label>
                        <input type="password" name="passwordC" value={formData.passwordC} onChange={handleChange} />
                      </div>
                    </>
                  )}
                </div>
                <div style={{ marginTop: '20px' }}>
                  <button type="submit" className="btn btn-primary">Simpan</button>
                  <button type="button" className="btn btn-secondary" style={{ marginLeft: '10px' }} onClick={() => {
                    setShowForm(false)
                    setEditingPegawai(null)
                  }}>Batal</button>
                </div>
              </form>
            </div>
          )}

          {showPhotoForm && (
            <div className="card" style={{ marginTop: '20px', background: '#f8f9fa' }}>
              <h3>Ubah Foto Pegawai: {selectedPegawai?.namaLengkap}</h3>
              <form onSubmit={handlePhotoSubmit}>
                <div className="form-group">
                  <label>Nama File *</label>
                  <input 
                    type="text" 
                    value={photoFileName} 
                    onChange={(e) => setPhotoFileName(e.target.value)} 
                    placeholder="contoh: foto-profil.jpg"
                    required 
                  />
                </div>
                <div className="form-group">
                  <label>Pilih File Foto *</label>
                  <input 
                    type="file" 
                    accept="image/*" 
                    onChange={handlePhotoChange}
                    required 
                  />
                </div>
                {photoError && <div className="error">{photoError}</div>}
                <div style={{ marginTop: '20px' }}>
                  <button type="submit" className="btn btn-primary" disabled={photoLoading}>
                    {photoLoading ? 'Mengunggah...' : 'Unggah Foto'}
                  </button>
                  <button 
                    type="button" 
                    className="btn btn-secondary" 
                    style={{ marginLeft: '10px' }} 
                    onClick={() => {
                      setShowPhotoForm(false)
                      setSelectedPegawai(null)
                      setPhotoFile(null)
                      setPhotoFileName('')
                      setPhotoError('')
                    }}>
                    Batal
                  </button>
                </div>
              </form>
            </div>
          )}

          {loading ? (
            <p>Loading...</p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Nama Lengkap</th>
                  <th>Email</th>
                  <th>Jabatan</th>
                  <th>Departemen</th>
                  <th>Tanggal Lahir</th>
                  <th>Aksi</th>
                </tr>
              </thead>
              <tbody>
                {pegawaiList.map(pegawai => (
                  <tr key={pegawai.idUser}>
                    <td>{pegawai.namaLengkap}</td>
                    <td>{pegawai.email}</td>
                    <td>{pegawai.namaJabatan || '-'}</td>
                    <td>{pegawai.namaDepartemen || '-'}</td>
                    <td>{formatEpoch(pegawai.tanggalLahir)}</td>
                    <td>
                      <button className="btn btn-secondary" onClick={() => handleEdit(pegawai)} style={{ marginRight: '5px' }}>Edit</button>
                      <button className="btn btn-secondary" onClick={() => handleUbahPhoto(pegawai)}>Ubah Foto</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  )
}

export default PegawaiList

