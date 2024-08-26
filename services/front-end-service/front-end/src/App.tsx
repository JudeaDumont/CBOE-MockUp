import './App.css';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import React from 'react';
import Login from './components/login';
import Profile from './components/profile';
import CreateUser from "./components/createUser.tsx";
import Landing from "./components/landing.tsx";
import Index from "./components"; // This will be the new component for the landing page

const App: React.FC = () => {
    const token = localStorage.getItem('jwtToken');

    return (
        <Router>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/create-user" element={<CreateUser />} />
                <Route path="/profile" element={<Profile />} />
                <Route path="/" element={token ? <Profile /> : <Landing />} />
                <Route path="/index" element={<Index />} />
            </Routes>
        </Router>
    );
};

export default App;
