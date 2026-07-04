import {useState} from 'react';

export const useChartState = () => {
    const [activeDate, setActiveDate] = useState(null);
    const [selectedDate, setSelectedDate] = useState(null);
    const [hoveredPurple, setHoveredPurple] = useState(null);

    return {
        activeDate,
        setActiveDate,
        selectedDate,
        setSelectedDate,
        hoveredPurple,
        setHoveredPurple
    };
};