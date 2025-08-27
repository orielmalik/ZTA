const pino = require('pino');
const fs = require('fs');
const path = require('path');
const {logarr} = require("../Utils/consts");

class AsyncLogger {
    constructor(logDir = './logs', maxFileSizeMB = 10) {
        this.logDir = logDir;
        this.maxFileSize = maxFileSizeMB * 1024 * 1024; // bytes
        this.currentFileIndex = 0;
        this.currentStream = null;
        this.logger = null;
    }

    _getLogFileName(index) {
        return path.join(this.logDir, `service-${index}.log`);
    }

    async _createStream() {
        if (!fs.existsSync(this.logDir)) {
            fs.mkdirSync(this.logDir);
        }

        const filename = this._getLogFileName(this.currentFileIndex);

        // create a write stream to file
        this.currentStream = fs.createWriteStream(filename, { flags: 'a' });

        this.logger = pino({}, this.currentStream);
    }

    async initLogger() {
        await this._createStream();
        this.logger.info('Logger initialized');
    }

    async _checkRotate() {
        const filename = this._getLogFileName(this.currentFileIndex);
        try {
            const stats = fs.statSync(filename);
            if (stats.size >= this.maxFileSize) {
                this.currentStream.end();
                this.currentFileIndex++;
                await this._createStream();
                this.logger.info('Log rotated to new file');
            }
        } catch {
            // file might not exist yet, ignore
        }
    }

    async writeLog(level, message) {
        if (!logarr.includes(level.toString().toLowerCase())) {
            throw new Error(`Unknown log level: ${level}`);
        }
        if (!this.logger) {
            await this.initLogger();
        }

        await this._checkRotate();


        this.logger[level](message);
    }
}

module.exports = AsyncLogger;
