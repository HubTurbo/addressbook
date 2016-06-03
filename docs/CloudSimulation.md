In order to simulate the idea of a cloud, we have implemented 2 key components: `CloudService` and `CloudSimulator`

They are arranged as follows:
> ... -> SyncManager <---> CloudService <---> CloudSimulator

# Key responsibilities
## CloudService
- Provides a high-level API for the local model to communicate with the server without having to be involved in too many details
- Responsible for converting API calls into the cloud calls
- Responsible for converting cloud responses into localised responses
  - Parses the stream of the content returned from the cloud (which is originally in JSON format). This applies to both the header and the body content
- Deals with both local model and service model, and converts between them when needed
- Returns an `ExtractedCloudResponse`, which contains the limit details as well as the returned (and converted) content that has been instantiated in memory

## CloudSimulator
- Provides a similar set of API as GitHub
- Writes the content of addressbooks into individual files, based on their names
  - For example, an addressbook `myContacts` will be written into `cloud/myContacts` and an addressbook `hisContacts` will be written into `cloud/hisContacts`.
- Maintains the API count of the current session
  - Similar to GitHub (authenticated), it has an API limit of 5000 by default, and resets at every hour's mark
  - The reset is based on a `TickingTimer` running in the background i.e. the reset is run after a given time delay
- Responses are returned as `RawCloudResponse`
  - ALWAYS (almost) contains the rate limit information. e.g. rate limit remaining and its next reset time
    - The only time it doesn't is if there is an `internal server error` (500)
  - ALWAYS contains the response code that indicates the status of the request. This response code is similar to its respective HTTP status code
  - May contain content such as a `CloudPerson` object in JSON form, depending on the query and its success status
- If initialised with a `true` boolean, there will be chance of data modifications before the result is returned
  - For lists, there may be `additions` and `modifications`
  - For single objects, there may be `modifications`
  - `additions` and `modifications` are both considered `mutations`

# Notes
## Response code
- Behaves similarly to `GitHub`
  - `20x` if request is successful. `x` depends on the type of request
  - `304` if there are no change since the last response
  - `400 - Bad Request` if the arguments given are invalid
  - `403 - Forbidden` if there is no more API quota left
  - `500 - Internal Server Error` if there is an error on the cloud. e.g. error reading from the cloud file

## CloudPerson
- Whenever its field are updated, `lastUpdatedAt` will also be set to the current time.
- Whenever it is deleted, it still remains in the list with `isDeleted` set to true

## CloudTag
- Whenever it is deleted, it is completely removed from the list of tags
- Does not have a `lastUpdatedAt` field

# Other notes related to HT
-  `Last-Modified` and `If-Modified-Since` headers do not seem to be useful

`Last-Modified` (response) and `If-Modified-Since` (request) headers seem to only apply to single-objects that have the `updatedAt` field (`issues` and `milestones`). The `Last-Modified` header field will also have the same value as `updatedAt`. This means that these do not provide us more information than we already have.

- We can check updates for `labels` and `collaborators` without incurring API usage

We need to simply save the `ETag` of the request and use it in further calls. If the response is that they are `304 Not Modified`, then it will not incur API usage.

- We are able to reduce the number of API calls for getting updated issues
For `issues`, `milestones` and `issue comments`, we can try to do something similar to `labels` and `collaborators` (see above). However, since the requests are paged, we have increased workload if we were to use `ETag`s to check for updates. For example, having 1000 issues will require up to 10 requests using the old `ETag`s, to ensure that none of the pages have changed. Results will also have to in descending order, since new issues will change all pages' `ETag`s if in ascending order)

We can instead just make a new `updated-since` request based on the date saved together with the last ETag. Any update requests will result in **>=1 (number of updated issues/issues per page)** API usage. Even if there are no updates, 1 API quota will be used.

Note: HT has not implemented retrieval of `issue comments` yet. The indicator we have now is simply a count, which is included in the issue's information.
