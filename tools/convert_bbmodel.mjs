import fs from "node:fs";
import path from "node:path";

const [input, output] = process.argv.slice(2);
if (!input || !output) {
    throw new Error("Usage: node tools/convert_bbmodel.mjs <input.bbmodel> <output.json>");
}

const source = JSON.parse(fs.readFileSync(input, "utf8"));
const textures = {};
for (let i = 0; i < source.textures.length; i++) {
    const texture = source.textures[i];
    const name = path.basename(texture.name, path.extname(texture.name));
    const folder = texture.folder === "blocks" ? "block" : "item";
    textures[String(i)] = `${texture.namespace || "rwbym"}:${folder}/${name}`;
}
textures.particle = textures["0"];

function bakeQuarterTurn(element, axis, degrees) {
    const radians = degrees * Math.PI / 180;
    const origin = element.origin || [8, 8, 8];
    const points = [];
    for (const x of [element.from[0], element.to[0]]) {
        for (const y of [element.from[1], element.to[1]]) {
            for (const z of [element.from[2], element.to[2]]) {
                const point = [x - origin[0], y - origin[1], z - origin[2]];
                let rotated;
                if (axis === "x") {
                    rotated = [point[0], point[1] * Math.cos(radians) - point[2] * Math.sin(radians),
                        point[1] * Math.sin(radians) + point[2] * Math.cos(radians)];
                } else if (axis === "y") {
                    rotated = [point[0] * Math.cos(radians) + point[2] * Math.sin(radians), point[1],
                        -point[0] * Math.sin(radians) + point[2] * Math.cos(radians)];
                } else {
                    rotated = [point[0] * Math.cos(radians) - point[1] * Math.sin(radians),
                        point[0] * Math.sin(radians) + point[1] * Math.cos(radians), point[2]];
                }
                points.push(rotated.map((value, index) => value + origin[index]));
            }
        }
    }
    element.from = [0, 1, 2].map(index => Math.min(...points.map(point => point[index])));
    element.to = [0, 1, 2].map(index => Math.max(...points.map(point => point[index])));
}

const elements = source.elements.map(raw => {
    const element = {
        name: raw.name,
        from: raw.from,
        to: raw.to,
        faces: {},
    };
    for (const [direction, face] of Object.entries(raw.faces || {})) {
        element.faces[direction] = {
            uv: face.uv,
            texture: `#${face.texture}`,
        };
        if (face.rotation) {
            element.faces[direction].rotation = face.rotation;
        }
    }
    const rotation = raw.rotation || [0, 0, 0];
    const axisIndex = rotation.findIndex(value => Math.abs(value) > 0.0001);
    if (axisIndex >= 0) {
        const angle = rotation[axisIndex];
        const axis = ["x", "y", "z"][axisIndex];
        if (Math.abs(angle) === 90) {
            bakeQuarterTurn(element, axis, angle);
        } else {
            element.rotation = {
                angle,
                axis,
                origin: raw.origin || [8, 8, 8],
            };
        }
    }
    return element;
});

const model = {
    credit: "Converted from the original RWBYM Blockbench model",
    textures,
    elements,
    display: source.display,
};
fs.writeFileSync(output, `${JSON.stringify(model, null, 2)}\n`);
