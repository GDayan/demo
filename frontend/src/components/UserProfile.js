import React, { useState, useEffect } from 'react';
import axios from 'axios';

function UserProfile({ token }) {
    const [user, setUser] = useState(null);
    const [formData, setFormData] = useState({
        email: '',
        firstName: '',
        lastName: '',
        password: '',
    });
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const response = await axios.get(`${process.env.REACT_APP_API_URL}/api/users/me`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setUser(response.data);
                setFormData({
                    email: response.data.email,
                    firstName: response.data.firstName || '',
                    lastName: response.data.lastName || '',
                    password: '',
                });
            } catch (err) {
                setError('Failed to fetch user data');
            }
        };
        fetchUser();
    }, [token]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleUpdate = async (e) => {
        e.preventDefault();
        try {
            await axios.put(
                `${process.env.REACT_APP_API_URL}/api/users/${user.id}`,
                formData,
                { headers: { Authorization: `Bearer ${token}` } }
            );
            setUser({ ...user, ...formData });
            setError('');
        } catch (err) {
            setError('Failed to update user');
        }
    };

    const handleDelete = async () => {
        if (window.confirm('Are you sure you want to delete your account?')) {
            try {
                await axios.delete(`${process.env.REACT_APP_API_URL}/api/users/${user.id}`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                window.location.href = '/login';
            } catch (err) {
                setError('Failed to delete user');
            }
        }
    };

    if (!user) return <div>Loading...</div>;

    return (
        <div className="max-w-md mx-auto mt-10 p-6 bg-white rounded shadow">
            <h2 className="text-2xl mb-4">User Profile</h2>
            {error && <p className="text-red-500">{error}</p>}
            <form onSubmit={handleUpdate}>
                <div className="mb-4">
                    <label className="block mb-1">Username</label>
                    <input
                        type="text"
                        value={user.username}
                        disabled
                        className="w-full p-2 border rounded bg-gray-100"
                    />
                </div>
                <div className="mb-4">
                    <label className="block mb-1">Email</label>
                    <input
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        className="w-full p-2 border rounded"
                        required
                    />
                </div>
                <div className="mb-4">
                    <label className="block mb-1">First Name</label>
                    <input
                        type="text"
                        name="firstName"
                        value={formData.firstName}
                        onChange={handleChange}
                        className="w-full p-2 border rounded"
                    />
                </div>
                <div className="mb-4">
                    <label className="block mb-1">Last Name</label>
                    <input
                        type="text"
                        name="lastName"
                        value={formData.lastName}
                        onChange={handleChange}
                        className="w-full p-2 border rounded"
                    />
                </div>
                <div className="mb-4">
                    <label className="block mb-1">New Password (optional)</label>
                    <input
                        type="password"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        className="w-full p-2 border rounded"
                    />
                </div>
                <button type="submit" className="w-full bg-blue-600 text-white p-2 rounded mb-2">
                    Update Profile
                </button>
                <button
                    type="button"
                    onClick={handleDelete}
                    className="w-full bg-red-600 text-white p-2 rounded"
                >
                    Delete Account
                </button>
            </form>
        </div>
    );
}

export default UserProfile;