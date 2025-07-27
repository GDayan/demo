import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import UserProfile from './components/UserProfile';
import AdminPanel from './components/AdminPanel';

function App() {
    const [token, setToken] = useState(localStorage.getItem('token') || '');
    const [userRole, setUserRole] = useState(localStorage.getItem('role') || '');

    useEffect(() => {
        if (token) {
            localStorage.setItem('token', token);
            localStorage.setItem('role', userRole);
        } else {
            localStorage.removeItem('token');
            localStorage.removeItem('role');
        }
    }, [token, userRole]);

    const handleLogout = () => {
        setToken('');
        setUserRole('');
    };

    return (
        <Router>
            <div className="min-h-screen bg-gray-100">
                <nav className="bg-blue-600 p-4 text-white flex justify-between">
                    <div>Krainet Test Assignment</div>
                    {token && (
                        <div>
                            <span className="mr-4">Role: {userRole}</span>
                            <button onClick={handleLogout} className="bg-red-500 px-4 py-2 rounded">
                                Logout
                            </button>
                        </div>
                    )}
                </nav>
                <Routes>
                    <Route path="/login" element={<Login setToken={setToken} setUserRole={setUserRole} />} />
                    <Route path="/register" element={<Register />} />
                    <Route
                        path="/profile"
                        element={token ? <UserProfile token={token} /> : <Navigate to="/login" />}
                    />
                    <Route
                        path="/admin"
                        element={
                            token && userRole === 'ADMIN' ? (
                                <AdminPanel token={token} />
                            ) : (
                                <Navigate to="/login" />
                            )
                        }
                    />
                    <Route path="/" element={<Navigate to="/login" />} />
                </Routes>
            </div>
        </Router>
    );
}

export default App;