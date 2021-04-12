import Foundation
import ObjectiveGit

struct HttpsGitClient: GitClient {
    func clone(from remoteURL: URL, to localURL: URL, credentials: GitClient.Credentials, progress: ((Float) -> Void)?, completion: @escaping (Error?) -> Void) {
        DispatchQueue.global(qos: .userInitiated).async {
            do {
                let credential = try GTCredential(userName: credentials.username, password: credentials.token)
                let auth = GTCredentialProvider { _, _, _ in credential }
                try GTRepository.clone(from: remoteURL, toWorkingDirectory: localURL, options: [GTRepositoryCloneOptionsCredentialProvider: auth]) { progressPointer, _ in
                    progress?(Float(progressPointer.pointee.received_objects) / Float(progressPointer.pointee.total_objects))
                }
                completion(nil)
            } catch {
                print(error)
                completion(error)
            }
        }
    }

    func fetch(repositoryURL: URL, credentials: Credentials, completion: @escaping (Error?) -> Void) {
        DispatchQueue.global(qos: .userInitiated).async {
            do {
                let repository = try GTRepository(url: repositoryURL)
                let origin = try GTRemote(name: "origin", in: repository)
                let credential = try GTCredential(userName: credentials.username, password: credentials.token)
                let auth = GTCredentialProvider { _, _, _ in credential }
                try repository.fetch(origin, withOptions: [GTRepositoryRemoteOptionsCredentialProvider: auth])
                completion(nil)
            } catch {
                print(error)
                completion(error)
            }
        }
    }

    func pull(repositoryURL: URL, credentials: Credentials, completion: @escaping (Error?) -> Void) {
        DispatchQueue.global(qos: .userInitiated).async {
            do {
                let repository = try GTRepository(url: repositoryURL)
                let currentBranch = try repository.currentBranch()
                let origin = try GTRemote(name: "origin", in: repository)
                let credential = try GTCredential(userName: credentials.username, password: credentials.token)
                let auth = GTCredentialProvider { _, _, _ in credential }
                try repository.pull(currentBranch, from: origin, withOptions: [GTRepositoryRemoteOptionsCredentialProvider: auth])
                completion(nil)
            } catch {
                print(error)
                completion(error)
            }
        }
    }

    func commitAll(repositoryURL: URL, message: String, author: Author) -> Error? {
        do {
            let repository = try GTRepository(url: repositoryURL)
            let index = try repository.index()
            try index.addAll()
            let tree = try index.writeTree()
            let signature = GTSignature(name: author.name, email: author.email, time: Date())!
            let parent = try repository.headReference().resolvedTarget as! GTCommit
            let branch = try repository.currentBranch()
            try repository.createCommit(with: tree, message: message, author: signature, committer: signature, parents: [parent], updatingReferenceNamed: branch.reference.name)
            return nil
        } catch {
            print(error)
            return error
        }
    }

    func push(repositoryURL: URL, credentials: Credentials, completion: @escaping (Error?) -> Void) {
        do {
            let repository = try GTRepository(url: repositoryURL)
            let branch = try repository.currentBranch()
            let origin = try GTRemote(name: "origin", in: repository)
            let credential = try GTCredential(userName: credentials.username, password: credentials.token)
            let auth = GTCredentialProvider { _, _, _ in credential }
            try repository.push(branch, to: origin, withOptions: [GTRepositoryRemoteOptionsCredentialProvider: auth])
            completion(nil)
        } catch {
            print(error)
            completion(error)
        }
    }

    func commitsAheadAndBehindOrigin(repositoryURL: URL) -> Result<(ahead: Int, behind: Int), Error> {
        do {
            let repository = try GTRepository(url: repositoryURL)
            let currentBranch = try repository.currentBranch()
            let remoteBranches = try repository.remoteBranches()
            let remoteBranch = remoteBranches.first { $0.shortName == currentBranch.shortName }!  // TODO handle error

            var ahead = 0
            var behind = 0
            try currentBranch.calculateAhead(&ahead, behind: &behind, relativeTo: remoteBranch)
            return .success((ahead, behind))
        } catch {
            print(error)
            return .failure(error)
        }
    }
}

extension GitManager {
    static let `default` = GitManager(gitClient: HttpsGitClient())
}
