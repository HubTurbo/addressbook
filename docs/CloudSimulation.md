In order to simulate the idea of a cloud, we have implemented 2 key components: `CloudService` and `CloudSimulator`

They are arranged as follows:
> ... -> SyncManager <---> CloudService <---> CloudSimulator

# Key responsibilities
## CloudService
- Provide a high-level API for the local model to communicate with the server without having to be involved in too many details
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
  - ALWAYS contains the rate limit information. e.g. rate limit remaining and its next reset time
  - ALWAYS contains the response code that indicates the status of the request. This response code is similar to its respective HTTP status code
  - May contain content such as a `CloudPerson` object in JSON form, depending on the query and its success status
- If initialised with a `true` boolean, there will be chance of data modifications before the result is returned
  - For lists, there may be `additions` and `modifications`
  - For single objects, there may be `modifications`
  - `additions` and `modifications` are considered under `mutations`

# Notes
## Response code
- Behaves similarly to `GitHub`
  - `20x` if request is successful. `x` depends on the type of request
  - `400` if the arguments given are invalid
  - `403` if there is no more API quota left
  - `500` if there is an error on the cloud. e.g. error reading from the cloud file

## CloudPerson
- Whenever its field are updated, `lastUpdatedAt` will also be set to the current time.
- Whenever it is deleted, it still remains in the list with `isDeleted` set to true

## CloudTag
- Whenever it is deleted, it is completely removed from the list of tags
- Does not have a `lastUpdatedAt` field
