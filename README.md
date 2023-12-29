# tennis

**WIP**

Libraries for the [**ATP**rotocol](https://atproto.com/) in Clojure.

## Usage

Follow functions available in core to see what you can do atm:

```clojure
(resolve-handle "https://api.bsky.app" "retr0.id")
;;=>
;; {:data {:did "did:plc:did-here"},
;;  :headers
;;  { ... }

(get-post-thread "https://api.bsky.app"
                 "at://did:plc:did-here/app.bsky.feed.post/3karfx5vrvv23")
;;=>
;; {:author "retr0.id",
;;  :name "David Buchanan",
;;  :text
;;  "apparently it's cybersecurity awareness month. you are now aware..."}

(post "https://any-pds-service.com"
      "your-handle.any-pds-service.com"
      "your-password"
      "What a lovely day!")
;;=>
;; {:data
;;  {:uri
;;   "at://did:plc:your-did-here/app.bsky.feed.post/3khomp3o6pc2n",
;;   :cid
;;   "bafyreicvmz4xjqt7j7uzi7vpzhxsfkkdaoh3vvucs76usvtwoedtgpep6u"},
;;  :headers
;;  { .. }
```

## License

Copyright © 2023 Robert Oliveros

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
