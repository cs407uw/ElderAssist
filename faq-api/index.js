const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');

const app = express();
const port = 3000;

// Middleware
app.use(bodyParser.json());
app.use(cors());

// Sample FAQs (in-memory data)
const faqs = [];

// Routes
// Get all FAQs
app.get('/faqs', (req, res) => {
    if (faqs.length === 0) {
        return res.status(200).json({ message: "No FAQs available" });
    }
    res.json(faqs);
});

// Get a single FAQ by ID
app.get('/faqs/:id', (req, res) => {
    const faq = faqs.find(f => f.id === req.params.id);
    if (faq) {
        res.json(faq);
    } else {
        res.status(404).json({ error: "FAQ not found" });
    }
});

// Create a new FAQ
app.post('/faqs', (req, res) => {
    const { question, answer } = req.body;
    if (!question || !answer) {
        return res.status(400).json({ error: "All fields (question, answer) are required" });
    }
    const newFaq = { id: uuidv4(), question, answer };
    faqs.push(newFaq);
    res.status(201).json(newFaq);
});

// Update an FAQ
app.put('/faqs/:id', (req, res) => {
    const faq = faqs.find(f => f.id === req.params.id);
    if (faq) {
        Object.assign(faq, req.body);
        res.json(faq);
    } else {
        res.status(404).json({ error: "FAQ not found" });
    }
});

// Delete an FAQ
app.delete('/faqs/:id', (req, res) => {
    const index = faqs.findIndex(f => f.id === req.params.id);
    if (index !== -1) {
        faqs.splice(index, 1);
        res.status(204).send();
    } else {
        res.status(404).json({ error: "FAQ not found" });
    }
});

// Error Handling Middleware
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({ error: "Something went wrong!" });
});

// Start the server
app.listen(port, () => {
    console.log(`Server is running on http://localhost:${port}`);
});
