// app.js
const express = require('express');
const AsyncLogger = require('./Logger/logger');
const {WriteCases}=require('./Utils/consts')
const app = express();
const port = 8088;


app.use(express.json());

const logger = new AsyncLogger();



app.put('/write', async (req, res) => {
    const { type, message } = req.query;

    const ip = req.headers['x-forwarded-for']?.split(',')[0] ||
        req.socket.remoteAddress;
    await logger.writeLog(type, " {ip address } "+ip);
    if (!type || !message) {
        return res.status(400).send(WriteCases[0]);
    }
    let typ = (type || '').toLowerCase().replace(/\s+/g, '');

    try {
        await logger.writeLog(typ, message);
        res.status(200).send(WriteCases[1]);
    } catch (err) {
        res.status(500).send(WriteCases[2](err));
        console.log(err.message);
    }
});

app.listen(port, async () => {
    await logger.initLogger();
    console.log(`Server listening on http://localhost:${port}`);
});
