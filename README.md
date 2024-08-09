# Github API service
This project provides API to connect with github and list repositories according to acceptance criteria.
## Acceptance criteria
As an api consumer, given username and header “Accept: application/json”, I would like to list all his github repositories, which are not forks. Information, which I require in the response, is:

```
Repository Name
Owner Login
For each branch it’s name and last commit sha
```

As an api consumer, given not existing github user, I would like to receive 404 response in such a format:
```
{
    “status”: ${responseCode}
    “message”: ${whyHasItHappened}
}
```
## Components
### Config
```WebClientConfig``` Configuration class for WebClient Bean.
### Controllers
```GithubController``` Main controller for handling incoming HTTP requests.
### DTO
```BranchResponse``` DTO containing branch information specified in acceptance criteria.
```RepoResponse``` DTO containing Repository information specified in acceptance criteria.
### Exception
```ErrorResponse``` record indicating what is in the response for 404 User not found Error.
```UserNotFoundException``` Exception inheriting from RuntimeException that is thrown when specified user is not found.
```GlobalExceptionHandler``` ControllerAdvice class for handling errors.
### Models
```Branch```: Represents a branch in a repository.
```Commit```: Represents a commit in a repository.
```Owner```: Represents the owner of a repository.
```Repository```: Represents a repository.
### Services
```GithubSerbice``` main service that fetches data from GitHub API.
## Testing
Application uses WireMock for validating compilance with acceptance criteria.
