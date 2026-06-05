export interface MinecraftTextSegment {
  text: string;
  classes: string[];
}

export function parseMinecraftFormatting(line: string): MinecraftTextSegment[] {
  const segments: MinecraftTextSegment[] = [];
  let classes: string[] = [];
  let buffer = '';
  const flush = () => {
    if (!buffer) return;
    segments.push({ text: buffer, classes: [...classes] });
    buffer = '';
  };
  for (let i = 0; i < line.length; i++) {
    if (line[i] === '§' && i + 1 < line.length) {
      flush();
      const code = line[++i].toLowerCase();
      if (code === 'r') {
        classes = [];
      } else if ('0123456789abcdef'.includes(code)) {
        classes = classes.filter((c) => !c.startsWith('mc-color-'));
        classes.push(`mc-color-${code}`);
      } else if (code === 'l') {
        if (!classes.includes('mc-bold')) classes.push('mc-bold');
      } else if (code === 'm') {
        if (!classes.includes('mc-strikethrough')) classes.push('mc-strikethrough');
      } else if (code === 'n') {
        if (!classes.includes('mc-underline')) classes.push('mc-underline');
      } else if (code === 'o') {
        if (!classes.includes('mc-italic')) classes.push('mc-italic');
      }
    } else {
      buffer += line[i];
    }
  }
  flush();
  return segments;
}
