import React, { useState, useEffect } from 'react';
import axios from 'axios';

function AdminPanel({ token }) {
    const [users, setUsers] = useState([]);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const response = await axios.get(`${process.env.REACT_APP_API_URL}/api/users`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setUsers(response.data);
            } catch (err) {
                setError('Failed to fetch users');
            }
        };
        fetchUsers();
    }, [token]);

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this user?')) {
            try {
                await axios.delete(`${process.env.REACT_APP_API_URL}/api/users/${id}`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setUsers(users.filter((user) => user.id !== id));
            } catch (err) {
                setError('Failed to delete user');
            }
        }
    };

    return (
        <div className="max-w-4xl mx-auto mt-10 p-6 bg-white rounded shadow">
            <h2 className="text-2xl mb-4">Admin Panel</h2>
            {error && <p className="text-red-500">{error}</p>}
            <table className="w-full border-collapse">
                <thead>
                <tr className="bg-gray-200">
                    <th className="border p-2">ID</th>
                    <th className="border p-2">Username</th>
                    <th className="border p-2">Email</th>
                    <th className="border p-2">Role</th>
                    <th className="border p-2">Actions</th>
                </tr>
                </thead>
                <tbody>
                {users.map((user) => (
                    <tr key={user.id}>
                        <td className="border p-2">{user.id}</td>
                        <td className="border p-2">{user.username}</td>
                        <td className="border p-2">{user.email}</td>
                        <td className="border p-2">{user.role}</td>
                        <td className="border p-2">
                            <button
                                onClick={() => handleDelete(user.id)}
                                className="bg-red-600 text-white px-4 py-1 rounded"
                            >
                                Delete
                            </button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default AdminPanel;