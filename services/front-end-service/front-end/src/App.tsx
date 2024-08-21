import './App.css'
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import React from "react";
import Login from "./components/login.tsx";
import Profile from "./components/profile.tsx";

const App: React.FC = () => {
    return (
        <Router>
            <Routes>
                <Route path="/login" element={<Login/>}/>
                <Route path="/profile" element={<Profile/>}/>
                <Route path="/" element={<Login/>}/>
            </Routes>
        </Router>
    );
};

export default App
