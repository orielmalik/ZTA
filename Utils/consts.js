const  logarr=['info', 'warn', 'error', 'debug', 'fatal'];
const WriteCases=['Missing level or message query parameters','Logged successfully',
    (err)=>`Logging error: ${err.message}`]

module.exports = {
    logarr,WriteCases
};
