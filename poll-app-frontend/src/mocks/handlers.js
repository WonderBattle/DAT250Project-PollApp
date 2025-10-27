// src/mocks/handlers.js
import { http, HttpResponse } from "msw";

const mockPolls = [
    {
        id: 1,
        question: "What is your favorite programming language?",
        createdBy: "Alice",
        createdAt: "2025-10-25T12:00:00Z",
        publishedAt: "2025-10-25T12:00:00Z",
        validUntil: "2025-12-31T23:59:59Z",
        options: [
            { text: "JavaScript", votes: 5 },
            { text: "Python", votes: 3 },
            { text: "Java", votes: 2 },
        ],
    },
    {
        id: 2,
        question: "Which framework do you use the most?",
        createdBy: "Bob",
        createdAt: "2025-10-20T10:00:00Z",
        publishedAt: "2025-10-20T10:00:00Z",
        validUntil: "2025-12-31T23:59:59Z",
        options: [
            { text: "React", votes: 6 },
            { text: "Vue", votes: 2 },
            { text: "Angular", votes: 1 },
        ],
    },
];

export const handlers = [
    // Get all polls
    http.get("http://localhost:8080/polls", () => {
        return HttpResponse.json(mockPolls);
    }),

    // Get a single poll by ID
    http.get("http://localhost:8080/polls/:pollId", ({ params }) => {
        const poll = mockPolls.find((p) => p.id === Number(params.pollId));
        return poll
            ? HttpResponse.json(poll)
            : HttpResponse.json({ message: "Not found" }, { status: 404 });
    }),

    // Delete a poll
    http.delete("http://localhost:8080/polls/:pollId", ({ params }) => {
        const index = mockPolls.findIndex((p) => p.id === Number(params.pollId));
        if (index !== -1) mockPolls.splice(index, 1);
        return HttpResponse.json({ success: true });
    }),

    // Add a new poll
    http.post("http://localhost:8080/polls", async ({ request }) => {
        const newPoll = await request.json();
        newPoll.id = mockPolls.length + 1;
        newPoll.createdAt = new Date().toISOString();
        mockPolls.push(newPoll);
        return HttpResponse.json(newPoll, { status: 201 });
    }),
];