import SwiftUI

/// Card showing the details of an entry. Shows title, author(s), and abstract, followed by all other values.
struct EntryCard: View {
    var entry: Entry

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(alignment: .leading, spacing: 20) {
                VStack(alignment: .center, spacing: 6) {
                    if let dateString = entry.dateString {
                        Text(dateString)
                            .font(.callout)
                            .foregroundColor(.secondary)
                    }
                    Text(entry.title ?? "No Title")
                        .font(.headline)
                        .multilineTextAlignment(.center)
                }
                .frame(maxWidth: .infinity)
                if let author = entry.author {
                    CardValue(title: "Author" + (author.contains("and") ? "s" : "")) {
                        Text(author)
                    }
                }
                if let abstract = entry.abstract {
                    CardValue(title: "Abstract") {
                        Text(abstract)
                    }
                }
                ForEach(entry.fields.sorted { $0.key < $1.key }, id: \.key) { key, value in
                    CardValue(title: key) {
                        // Values starting with "http" are shown as tappable buttons.
                        if value.starts(with: "http") {
                            if let url = URL(string: value) {
                                Button {
                                    UIApplication.shared.open(url, options: [:])
                                } label: {
                                    Text(value)
                                        .lineLimit(1)
                                }
                            } else {
                                Text(value)
                                    .lineLimit(1)
                            }
                        } else {
                            Text(value)
                        }
                    }
                }
            }
            .padding(20)
        }
        .background(RoundedRectangle(cornerRadius: 20).fill(Color(.secondarySystemGroupedBackground)))
        .overlay(RoundedRectangle(cornerRadius: 20).strokeBorder(Color(.separator), lineWidth: 1 / UIScreen.main.scale))
    }
}
