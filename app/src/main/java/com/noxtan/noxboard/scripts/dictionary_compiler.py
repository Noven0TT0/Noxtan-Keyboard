import os
import struct

class TrieNode:
    def __init__(self, char):
        self.char = char
        self.children = {}
        self.is_end = False
        self.freq = 0
        self.index = -1
        self.child_start_idx = -1

class Trie:
    def __init__(self):
        self.root = TrieNode('')

    def insert(self, word, freq):
        curr = self.root
        for char in word:
            if char not in curr.children:
                curr.children[char] = TrieNode(char)
            curr = curr.children[char]
        curr.is_end = True
        curr.freq += freq

def build_trie_from_txt(txt_path):
    trie = Trie()
    if not os.path.exists(txt_path):
        print(f"Error: File not found at {txt_path}")
        return None

    with open(txt_path, 'r', encoding='utf-8') as f:
        for line in f:
            parts = [p.strip() for p in line.split(',')]
            if len(parts) >= 3:
                w1 = parts[0]
                w2 = parts[1]
                freq = int(parts[2]) if parts[2].isdigit() else 1
                # Unigrams
                trie.insert(w1, freq)
                trie.insert(w2, freq)
                # Bigrams (Next-word prediction အတွက် Space ခြားပြီး ထည့်သည်)
                trie.insert(f"{w1} {w2}", freq)
            elif len(parts) >= 1 and parts[0]:
                w1 = parts[0]
                freq = int(parts[1]) if len(parts) > 1 and parts[1].isdigit() else 1
                trie.insert(w1, freq)
    return trie

def flatten_and_save_trie(trie, output_path):
    nodes_array = []
    queue = [trie.root]
    trie.root.index = 0

    # BFS Flattening
    while queue:
        node = queue.pop(0)
        nodes_array.append(node)

        # C++ ဘက်မှာ Binary Search လုပ်လို့ရအောင် Character အလိုက် စီထားသည်
        sorted_children = sorted(node.children.values(), key=lambda x: x.char)

        if sorted_children:
            node.child_start_idx = len(nodes_array) + len(queue)
            for child in sorted_children:
                child.index = len(nodes_array) + len(queue)
                queue.append(child)

    # 16-bytes per node: < H (char:2) B (is_end:1) B (pad:1) i (freq:4) i (child_start:4) i (child_count:4)
    with open(output_path, 'wb') as f:
        # Total Node Count ကို Header အနေနဲ့ 4 bytes အရင်ရေးသည်
        f.write(struct.pack('<i', len(nodes_array)))

        for node in nodes_array:
            c_val = ord(node.char) if node.char else 0
            is_end_val = 1 if node.is_end else 0
            freq_val = node.freq
            child_start = node.child_start_idx
            child_count = len(node.children)

            f.write(struct.pack('<HBBiii', c_val, is_end_val, 0, freq_val, child_start, child_count))

    print(f"Successfully generated: {os.path.basename(output_path)} (Nodes: {len(nodes_array)})")

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    assets_dir = os.path.abspath(os.path.join(script_dir, '../../../../../assets'))

    if not os.path.exists(assets_dir):
        print(f"Error: Asset directory not found at {assets_dir}")
        return

    # proverbs_my ကိုပါ တစ်ခါတည်း ထုတ်ပေးရန် ထည့်ထားသည်
    dicts = ['dict_my', 'dict_en', 'dict_th', 'proverbs_my']

    for d in dicts:
        # txt ဖိုင်များကို script ရှိရာ folder (လက်ရှိနေရာ) မှ ဖတ်မည်
        txt_file = os.path.join(script_dir, f"{d}.txt")
        # ထွက်လာမည့် bin ဖိုင်များကို assets folder ထဲသို့ တိုက်ရိုက် ထည့်မည်
        bin_file = os.path.join(assets_dir, f"{d}.bin")

        if os.path.exists(txt_file):
            print(f"Processing {d}.txt ...")
            trie = build_trie_from_txt(txt_file)
            if trie:
                flatten_and_save_trie(trie, bin_file)
        else:
            if d != 'proverbs_my': # proverbs မရှိရင် error မပြဘဲ ကျော်သွားရန်
                print(f"Skipping {d}.txt (File not found in {script_dir})")

if __name__ == "__main__":
    main()