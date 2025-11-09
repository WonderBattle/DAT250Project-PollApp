const CreatePollCard = ({ onCancel }) => {
    //sample for card simulation
    return (
        <div className="poll-card">
            <h2>Create New Poll</h2>
            <div className="poll-buttons">
                <button className="save-btn">Save Poll</button>
                <button className="delete-btn" onClick={onCancel}>Cancel</button>
            </div>
        </div>
    );
};

export default CreatePollCard;