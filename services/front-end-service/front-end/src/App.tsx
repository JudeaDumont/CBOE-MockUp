import './App.css';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import React from 'react';
import Login from './components/login';
import Profile from './components/profile';
import Landing from './components/landing';
import CreateUser from "./components/createUser.tsx"; // This will be the new component for the landing page

const App: React.FC = () => {
    const token = localStorage.getItem('jwtToken');

    return (
        <Router>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/create-user" element={<CreateUser />} />
                <Route path="/profile" element={token ? <Profile /> : <Navigate to="/" />} />
                <Route path="/" element={token ? <Profile /> : <Landing />} />
            </Routes>
        </Router>
    );
};

export default App;
