import CoreData
import SwiftyBibtex

/// DB entity for entries in a project.
final class Entry: NSManagedObject, Identifiable {
    enum Decision: Int16 {
        case outstanding, keep, discard
    }

    private static let keysToRemove = Set(["abstract", "author", "classes", "month", "title", "year"])

    var id: String { citationKey }

    @NSManaged var citationKey: String

    @NSManaged var title: String?
    @NSManaged var author: String?
    @NSManaged var abstract: String?

    @NSManaged var year: Int16
    @NSManaged var month: Int16

    @NSManaged var rangeInFileData: Data
    @NSManaged var fieldsData: Data?

    @NSManaged var decisionRaw: Int16

    @NSManaged var classesChanged: Bool

    @NSManaged var project: Project
    @NSManaged var classes: Set<TaxonomyClass>

    var decision: Decision {
        get { Decision(rawValue: decisionRaw) ?? .outstanding }
        set { decisionRaw = newValue.rawValue }
    }

    var dateString: String? {
        return year == 0 ? nil : (month == 0 ? "" : "\(month)/") + "\(year)"
    }

    var rangeInFile: RangeInFile {
        get { try! PropertyListDecoder().decode(RangeInFile.self, from: rangeInFileData) }
        set { rangeInFileData = try! PropertyListEncoder().encode(newValue) }
    }

    var fields: [String: String] {
        get { fieldsData == nil ? [:] : try! PropertyListDecoder().decode([String: String].self, from: fieldsData!) }
        set { fieldsData = try! PropertyListEncoder().encode(newValue) }
    }

    var classesString: String {
        return project.sortedRootClasses.compactMap { $0.classesString(for: self) }.joined(separator: ", ")
    }

    @discardableResult static func newEntity(publication: Publication, decision: Decision = .outstanding, project: Project, in managedObjectContext: NSManagedObjectContext) -> Entry {
        let entry = NSEntityDescription.insertNewObject(forEntityName: String(describing: self), into: managedObjectContext) as! Entry
        entry.citationKey = publication.citationKey

        entry.title = publication.fields["title"]?.withLatexMacrosReplaced
        entry.author = publication.fields["author"]?.withLatexMacrosReplaced
        entry.abstract = publication.fields["abstract"]

        if let yearString = publication.fields["year"], let year = Int16(yearString) {
            entry.year = year
        } else {
            entry.year = 0
        }
        if let monthString = publication.fields["month"] {
            if let month = Int16(monthString) {
                entry.month = month
            }
            let months = ["january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december"]
            for (i, month) in months.enumerated() {
                if monthString == month || monthString == month.prefix(3) {
                    entry.month = Int16(i + 1)
                    break
                }
            }
        } else {
            entry.month = 0
        }

        entry.rangeInFile = publication.rangeInFile
        entry.fields = publication.fields.filter { !Self.keysToRemove.contains($0.key) }

        entry.decision = decision

        entry.classesChanged = false

        entry.project = project

        return entry
    }

    static var fetchRequest: NSFetchRequest<Entry> {
        return NSFetchRequest(entityName: String(describing: self))
    }
}
