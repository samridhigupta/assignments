import nltk

__author__ = 'samridhi'
import pandas as pd


def popular_vote_feature(state):
    return {'state_name': state}


def contribution_feature(occupation):
    return {'occupation_name': occupation}


def winning_party(count, result_in_last_two_election):
    if count == 2:
        return result_in_last_two_election > 1
    return count > 2


def winning_party_in_contribution(a):
    return a[0] > a[1]


def winning_party_in_last_election(count):
    return count > 0


print("Classifier on popular vote in every state")

location = 'electionData.xlsx'
df = pd.read_excel(location, sheetname='Sheet1')

trainingData = []
testingData = []

democrat_contribution = {}
democrat_contribution2012 = {}
republic_contribution = {}
republic_contribution2012 = {}
for index, row in df.iterrows():
    democrat = 0
    resultInLastElection = 0
    resultInLastTwoElection = 0
    if row['Republic: 2000'] < row['Democrat: 2000']:
        democrat += 1
    if row['Republic: 2004'] < row['Democrat: 2004']:
        democrat += 1
    if row['Republic: 2008'] < row['Democrat: 2008']:
        democrat += 1
        resultInLastTwoElection += 1
    if row['Republic: 2012'] < row['Democrat: 2012']:
        democrat += 1
        resultInLastElection = 1
        resultInLastTwoElection += 1
    testingData.append((row['State'].encode('utf-8'), resultInLastElection))
    trainingData.append((row['State'].encode('utf-8'), democrat, resultInLastTwoElection))
    democrat_contribution[row['State'].encode('utf-8')] = 0
    republic_contribution[row['State'].encode('utf-8')] = 0
    democrat_contribution2012[row['State'].encode('utf-8')] = 0
    republic_contribution2012[row['State'].encode('utf-8')] = 0

train_set = [
    (popular_vote_feature(state), winning_party(democratVoteCount, resultInLastTwoElection)) for
    (state, democratVoteCount, resultInLastTwoElection) in trainingData]
test_set = [(popular_vote_feature(state), winning_party_in_last_election(resultInLastElection)) for
            (state, resultInLastElection)
            in testingData]
classifier = nltk.NaiveBayesClassifier.train(train_set)
print("accuracy")
print(nltk.classify.accuracy(classifier, test_set))
print train_set
print trainingData
democrat_contribution.pop("Totals", None)
republic_contribution.pop("Totals", None)

location = 'contriDataDemocrat2016.xlsx'
df = pd.read_excel(location, sheetname='Sheet1')
democrat_contribution_2016_df = df.copy(deep=True)


for key in democrat_contribution:
    democrat_contribution[key] = df.loc[df['state'] == key, 'amount'].sum()

location = 'contriDataRepublic2016.xlsx'
df = pd.read_excel(location, sheetname='Sheet1')
republic_contribution_2016_df = df.copy(deep=True)


for key in republic_contribution:
    republic_contribution[key] = df.loc[df['state'] == key, 'amount'].sum()

validationData = []
total_democrat_win = 0
total_democrat_contri = 0
total_republic_contri = 0
for key in republic_contribution:
    democrat_win = 0
    if republic_contribution[key] < democrat_contribution[key]:
        democrat_win = 1
        total_democrat_win += 1
    validationData.append((key, democrat_win))
    total_democrat_contri += democrat_contribution[key]
    total_republic_contri += republic_contribution[key]

validationSet = [(popular_vote_feature(state), winning_party_in_last_election(democrat_contribution)) for
                 (state, democrat_contribution) in validationData]
print("accuracy for ")
print("validating our model which was created using past data with contribution data")
print(nltk.classify.accuracy(classifier, validationSet))

print 'number of states where democrats have more contribution than republican for 2016'
print total_democrat_win
print 'total_democrat_contri for 2016'
print total_democrat_contri
print 'total_republic_contri for 2016'
print total_republic_contri

location = 'contriDataDemocrat2012.xlsx'
df = pd.read_excel(location, sheetname='Sheet1')

for key in democrat_contribution2012:
    democrat_contribution2012[key] = df.loc[df['state'] == key, 'amount'].sum()

location = 'contriDataRepublic2012.xlsx'
df = pd.read_excel(location, sheetname='Sheet1')

for key in republic_contribution2012:
    republic_contribution2012[key] = df.loc[df['state'] == key, 'amount'].sum()

validationData2012 = []
total_democrat_win2012 = 0
total_democrat_contri2012 = 0
total_republic_contri2012 = 0
for key in republic_contribution2012:
    democrat_win2012 = 0
    if republic_contribution2012[key] < democrat_contribution2012[key]:
        democrat_win2012 = 1
        total_democrat_win2012 += 1
    validationData2012.append((key, democrat_win2012))
    total_democrat_contri2012 += democrat_contribution2012[key]
    total_republic_contri2012 += republic_contribution2012[key]

validationSet2012 = [(popular_vote_feature(state), winning_party_in_last_election(democrat_contribution)) for
                     (state, democrat_contribution) in validationData2012]
print("accuracy for ")
print("validating our model which was created using past data with contribution data")
print(nltk.classify.accuracy(classifier, validationSet2012))

print 'number of states where democrats have more contribution than republican for 2012'
print total_democrat_win2012
print 'total_democrat_contri for 2012'
print total_democrat_contri2012
print 'total_republic_contri for 2012'
print total_republic_contri2012

testD = democrat_contribution_2016_df[::4]
groupD = testD.groupby(['occupation']).groups

testR = republic_contribution_2016_df[::4]
groupR = testR.groupby(['occupation']).groups

contribution_occupation = {}

for key in groupD:
    if key in groupR:
        contribution_occupation[key] = [len(groupD[key]), len(groupR[key])]
    else:
        contribution_occupation[key] = [len(groupD[key]), 0]

for key in groupR:
    if key not in groupD:
        contribution_occupation[key] = [0, len(groupR[key])]

trainingSetContribution = [(contribution_feature(key), winning_party_in_contribution(contribution_occupation[key])) for
                           key in contribution_occupation]
classifier = nltk.NaiveBayesClassifier.train(trainingSetContribution)


testD = democrat_contribution_2016_df[::7]
groupD = testD.groupby(['occupation']).groups

testR = republic_contribution_2016_df[::7]
groupR = testR.groupby(['occupation']).groups

contribution_occupation = {}

for key in groupD:
    if key in groupR:
        contribution_occupation[key] = [len(groupD[key]), len(groupR[key])]
    else:
        contribution_occupation[key] = [len(groupD[key]), 0]

for key in groupR:
    if key not in groupD:
        contribution_occupation[key] = [0, len(groupR[key])]

testingSetContribution = [(contribution_feature(key), winning_party_in_contribution(contribution_occupation[key])) for
                           key in contribution_occupation]
print("accuracy for testing data for contribution")
print(nltk.classify.accuracy(classifier, testingSetContribution))


testD = democrat_contribution_2016_df[::23]
groupD = testD.groupby(['occupation']).groups

testR = republic_contribution_2016_df[::23]
groupR = testR.groupby(['occupation']).groups

contribution_occupation = {}

for key in groupD:
    if key in groupR:
        contribution_occupation[key] = [len(groupD[key]), len(groupR[key])]
    else:
        contribution_occupation[key] = [len(groupD[key]), 0]

for key in groupR:
    if key not in groupD:
        contribution_occupation[key] = [0, len(groupR[key])]

validationSetContribution = [(contribution_feature(key), winning_party_in_contribution(contribution_occupation[key])) for
                           key in contribution_occupation]
print("accuracy for validation data for contribution")
print(nltk.classify.accuracy(classifier, validationSetContribution))

amtDemocrat = []
amtRepublic = []
amtRepublic = republic_contribution_2016_df[['amount']]
amtDemocrat = democrat_contribution_2016_df[['amount']]

poorR = 0
richR = 0
middleR = 0

poorD = 0
richD = 0
middleD = 0


for index in range(0, len(amtDemocrat)):
    amt = amtDemocrat[index]
    print amt
    if amt < 25:
        poorD += 1
    if amt > 1000:
        richD += 1
    if 25 < amt < 1000:
        middleD += 1


for amt in amtRepublic:
    if amt < 25:
        poorR += 1
    if amt > 1000:
        richR += 1
    if 25 < amt < 1000:
        middleR += 1

print poorR
print richR
print middleR

print poorD
print richD
print middleD
