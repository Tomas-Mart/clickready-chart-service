// Применяем оффсеты
export const applyOffset = (date, baseCoords, offsets) => {
    const base = baseCoords[date];
    const offset = offsets[date] || {cx: 0, cy: 0};
    return {
        cx: base.cx + offset.cx,
        cy: base.cy + offset.cy,
    };
};