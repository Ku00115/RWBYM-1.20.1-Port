import fs from "node:fs";
import path from "node:path";
import { spawnSync } from "node:child_process";

const root = process.cwd();
const modelDir = path.join(root, "src/main/resources/assets/rwbym/models/item");
const convert = path.join(root, "tools/convert_bbmodel.mjs");

const variants = {
    p90: [
        {
            suffix: "magout",
            predicate: { magout: 1 },
            transforms: [{ group: "magazine", visible: false }],
        },
        {
            suffix: "slideback",
            predicate: { slideback: 1 },
            transforms: [{ group: "charging bolt", translate: [-1.4, 0, 0] }],
        },
        {
            suffix: "ads",
            predicate: { ads: 1 },
            transforms: [
                { group: "frame", translate: [0, 0.2, 0] },
                { group: "barrel", translate: [0, 0.2, 0] },
                { group: "magazine", translate: [0, 0.2, 0] },
                { group: "trigger", translate: [0, 0.2, 0] },
                { group: "charging bolt", translate: [0, 0.2, 0] },
            ],
        },
        {
            suffix: "ads_magout",
            predicate: { ads: 1, magout: 1 },
            transforms: [
                { group: "frame", translate: [0, 0.2, 0] },
                { group: "barrel", translate: [0, 0.2, 0] },
                { group: "magazine", visible: false },
                { group: "trigger", translate: [0, 0.2, 0] },
                { group: "charging bolt", translate: [0, 0.2, 0] },
            ],
        },
        {
            suffix: "ads_slideback",
            predicate: { ads: 1, slideback: 1 },
            transforms: [
                { group: "frame", translate: [0, 0.2, 0] },
                { group: "barrel", translate: [0, 0.2, 0] },
                { group: "magazine", translate: [0, 0.2, 0] },
                { group: "trigger", translate: [0, 0.2, 0] },
                { group: "charging bolt", translate: [-1.4, 0.2, 0] },
            ],
        },
    ],
    hecate2: [
        {
            suffix: "magout",
            predicate: { magout: 1 },
            transforms: [{ group: "magazine", visible: false }],
        },
        {
            suffix: "boltback",
            predicate: { boltopen: 1 },
            transforms: [{ group: "bolt", translate: [-2.25, 0, 0] }],
        },
        {
            suffix: "ads",
            predicate: { ads: 1 },
            transforms: [
                { group: "frame", translate: [0, 0.35, 0] },
                { group: "bolt", translate: [0, 0.35, 0] },
                { group: "magazine", translate: [0, 0.35, 0] },
                { group: "scope", translate: [0, 0.35, 0] },
            ],
        },
        {
            suffix: "ads_magout",
            predicate: { ads: 1, magout: 1 },
            transforms: [
                { group: "frame", translate: [0, 0.35, 0] },
                { group: "bolt", translate: [0, 0.35, 0] },
                { group: "magazine", visible: false },
                { group: "scope", translate: [0, 0.35, 0] },
            ],
        },
        {
            suffix: "ads_boltback",
            predicate: { ads: 1, boltopen: 1 },
            transforms: [
                { group: "frame", translate: [0, 0.35, 0] },
                { group: "bolt", translate: [-2.25, 0.35, 0] },
                { group: "magazine", translate: [0, 0.35, 0] },
                { group: "scope", translate: [0, 0.35, 0] },
            ],
        },
    ],
};

for (const name of Object.keys(variants)) {
    runConvert(name);
    const basePath = path.join(modelDir, `${name}.json`);
    const base = readJson(basePath);
    const overrides = [];
    for (const variant of variants[name]) {
        const model = makeVariant(name, variant);
        const variantName = `${name}_${variant.suffix}`;
        fs.writeFileSync(path.join(modelDir, `${variantName}.json`), `${JSON.stringify(model, null, 2)}\n`);
        overrides.push({ predicate: variant.predicate, model: `rwbym:item/${variantName}` });
    }
    base.overrides = overrides;
    fs.writeFileSync(basePath, `${JSON.stringify(base, null, 2)}\n`);
}

function runConvert(name) {
    const result = spawnSync(process.execPath, [convert, path.join(modelDir, `${name}.bbmodel`),
        path.join(modelDir, `${name}.json`)], { stdio: "inherit" });
    if (result.status !== 0) {
        throw new Error(`Failed to convert ${name}.bbmodel`);
    }
}

function makeVariant(name, variant) {
    const source = readJson(path.join(modelDir, `${name}.bbmodel`));
    const elementIds = selectedElementIds(source, variant.transforms);
    const base = readJson(path.join(modelDir, `${name}.json`));
    const hiddenIds = new Set();
    const translations = [];
    for (const transform of variant.transforms) {
        const ids = selectedElementIds(source, [transform]);
        if (transform.visible === false) {
            ids.forEach(id => hiddenIds.add(id));
        }
        if (transform.translate) {
            translations.push({ ids, translate: transform.translate });
        }
    }
    base.elements = base.elements
        .filter((element, index) => !hiddenIds.has(source.elements[index]?.uuid))
        .map((element, index) => {
            const raw = source.elements[index];
            if (!raw || !elementIds.has(raw.uuid)) {
                return element;
            }
            const translation = translations.find(entry => entry.ids.has(raw.uuid));
            return translation ? translateElement(element, translation.translate) : element;
        });
    base.credit = `${base.credit}; generated special gun variant ${variant.suffix}`;
    return base;
}

function selectedElementIds(source, transforms) {
    const names = new Set(transforms.map(transform => transform.group));
    const ids = new Set();
    function walk(nodes, active = false) {
        for (const node of nodes || []) {
            if (typeof node === "string") {
                if (active) ids.add(node);
                continue;
            }
            const nextActive = active || names.has(node.name);
            walk(node.children, nextActive);
        }
    }
    walk(source.outliner);
    return ids;
}

function translateElement(element, delta) {
    const copy = structuredClone(element);
    copy.from = copy.from.map((value, index) => value + delta[index]);
    copy.to = copy.to.map((value, index) => value + delta[index]);
    if (copy.rotation?.origin) {
        copy.rotation.origin = copy.rotation.origin.map((value, index) => value + delta[index]);
    }
    return copy;
}

function readJson(file) {
    return JSON.parse(fs.readFileSync(file, "utf8"));
}
