function escapeHtml (unsafe) {
    return unsafe
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');
}

function translateMinecraftColor(color) {
    if (color.startsWith('#')) {
        return color;
    }

    const colors = {
        black: '#000000',
        dark_blue: '#0000AA',
        dark_green: '#00AA00',
        dark_aqua: '#00AAAA',
        dark_red: '#AA0000',
        dark_purple: '#AA00AA',
        gold: '#FFAA00',
        gray: '#AAAAAA',
        dark_gray: '#555555',
        blue: '#5555FF',
        green: '#55FF55',
        aqua: '#55FFFF',
        red: '#FF5555',
        light_purple: '#FF55FF',
        yellow: '#FFFF55',
        white: '#FFFFFF'
    };
    return colors[color] || color;
}

// Convert URLs to clickable links
function linkifyText(text) {
    const urlRegex = /(https?:\/\/[^\s]+)/g;
    return text.replace(urlRegex, url => `<a href="${url}" target="_blank">${url}</a>`);
}


function parseMinecraftText(json) {
    if (typeof json === 'string') {
        json = JSON.parse(json);
    }

    function formatTranslation(key, args) {
        const template = translations[key] || key;
        let index = 0;
        return template.replace(/%s/g, () => {
            const arg = args[index];
            index++;
            return formatComponent(arg);
        });
    }

    function formatComponent(component) {
        if (typeof component === 'string') {
            return linkifyText(escapeHtml(component));
        }
    
        let result = '';
        let style = '';
    
        // Handle styling
        if (component.color) {
            style += `color: ${translateMinecraftColor(component.color)};`;
        }
        if (component.bold) {
            style += 'font-weight: bold;';
        }
        if (component.italic) {
            style += 'font-style: italic;';
        }
        if (component.underlined) {
            style += 'text-decoration: underline;';
        }
    
        // Handle translations
        if (component.translate) {
            result = formatTranslation(component.translate, component.with || []);
        } else if (component.text) {
            result = linkifyText(escapeHtml(component.text));
        }
    
        // Wrap in span if we have styling
        if (style) {
            result = `<span style="${style}">${result}</span>`;
        }
    
        // Handle extra components
        if (component.extra) {
            result += component.extra.map(formatComponent).join('');
        }
    
        return result;
    }

    return formatComponent(json);
}
