/*
 * Copyright (c) 2020 Bytedance Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#ifndef FLATBUFFERS_GENERATED_REUSEMODELLEGACY_FASTBOTX_H_
#define FLATBUFFERS_GENERATED_REUSEMODELLEGACY_FASTBOTX_H_

#include "flatbuffers/flatbuffers.h"

namespace fastbotx {

    struct ActivityTimes;
    struct ActivityTimesBuilder;

    struct ReuseEntry;
    struct ReuseEntryBuilder;

    struct ReuseModel;
    struct ReuseModelBuilder;

    struct ActivityTimes FLATBUFFERS_FINAL_CLASS : private flatbuffers::Table {
        typedef ActivityTimesBuilder Builder;
        enum FlatBuffersVTableOffset FLATBUFFERS_VTABLE_UNDERLYING_TYPE {
            VT_ACTIVITY = 4,
            VT_TIMES = 6
        };

        const flatbuffers::String *activity() const {
            return GetPointer<const flatbuffers::String *>(VT_ACTIVITY);
        }

        int32_t times() const {
            return GetField<int32_t>(VT_TIMES, 0);
        }

        bool Verify(flatbuffers::Verifier &verifier) const {
            return VerifyTableStart(verifier) &&
                   VerifyOffset(verifier, VT_ACTIVITY) &&
                   verifier.VerifyString(activity()) &&
                   VerifyField<int32_t>(verifier, VT_TIMES) &&
                   verifier.EndTable();
        }
    };

    struct ActivityTimesBuilder {
        typedef ActivityTimes Table;
        flatbuffers::FlatBufferBuilder &fbb_;
        flatbuffers::uoffset_t start_;

        void add_activity(flatbuffers::Offset <flatbuffers::String> activity) {
            fbb_.AddOffset(ActivityTimes::VT_ACTIVITY, activity);
        }

        void add_times(int32_t times) {
            fbb_.AddElement<int32_t>(ActivityTimes::VT_TIMES, times, 0);
        }

        explicit ActivityTimesBuilder(flatbuffers::FlatBufferBuilder &_fbb)
                : fbb_(_fbb) {
            start_ = fbb_.StartTable();
        }

        flatbuffers::Offset <ActivityTimes> Finish() {
            const auto end = fbb_.EndTable(start_);
            auto o = flatbuffers::Offset<ActivityTimes>(end);
            return o;
        }
    };

    inline flatbuffers::Offset <ActivityTimes> CreateActivityTimes(
            flatbuffers::FlatBufferBuilder &_fbb,
            flatbuffers::Offset <flatbuffers::String> activity = 0,
            int32_t times = 0) {
        ActivityTimesBuilder builder_(_fbb);
        builder_.add_times(times);
        builder_.add_activity(activity);
        return builder_.Finish();
    }

    inline flatbuffers::Offset <ActivityTimes> CreateActivityTimesDirect(
            flatbuffers::FlatBufferBuilder &_fbb,
            const char *activity = nullptr,
            int32_t times = 0) {
        auto activity__ = activity ? _fbb.CreateString(activity) : 0;
        return fastbotx::CreateActivityTimes(
                _fbb,
                activity__,
                times);
    }

    struct ReuseEntry FLATBUFFERS_FINAL_CLASS : private flatbuffers::Table {
        typedef ReuseEntryBuilder Builder;
        enum FlatBuffersVTableOffset FLATBUFFERS_VTABLE_UNDERLYING_TYPE {
            VT_ACTION = 4,
            VT_TARGETS = 6
        };

        uint64_t action() const {
            return GetField<uint64_t>(VT_ACTION, 0);
        }

        bool KeyCompareLessThan(const ReuseEntry *o) const {
            return action() < o->action();
        }

        int KeyCompareWithValue(uint64_t val) const {
            return static_cast<int>(action() > val) - static_cast<int>(action() < val);
        }

        const flatbuffers::Vector <flatbuffers::Offset<fastbotx::ActivityTimes>> *targets() const {
            return GetPointer<const flatbuffers::Vector<flatbuffers::Offset<fastbotx::ActivityTimes>> *>(
                    VT_TARGETS);
        }

        bool Verify(flatbuffers::Verifier &verifier) const {
            return VerifyTableStart(verifier) &&
                   VerifyField<uint64_t>(verifier, VT_ACTION) &&
                   VerifyOffset(verifier, VT_TARGETS) &&
                   verifier.VerifyVector(targets()) &&
                   verifier.VerifyVectorOfTables(targets()) &&
                   verifier.EndTable();
        }
    };

    struct ReuseEntryBuilder {
        typedef ReuseEntry Table;
        flatbuffers::FlatBufferBuilder &fbb_;
        flatbuffers::uoffset_t start_;

        void add_action(uint64_t action) {
            fbb_.AddElement<uint64_t>(ReuseEntry::VT_ACTION, action, 0);
        }

        void add_targets(flatbuffers::Offset <flatbuffers::Vector<
                flatbuffers::Offset < fastbotx::ActivityTimes>>

        > targets) {
            fbb_.AddOffset(ReuseEntry::VT_TARGETS, targets);
        }

        explicit ReuseEntryBuilder(flatbuffers::FlatBufferBuilder &_fbb)
                : fbb_(_fbb) {
            start_ = fbb_.StartTable();
        }

        flatbuffers::Offset <ReuseEntry> Finish() {
            const auto end = fbb_.EndTable(start_);
            auto o = flatbuffers::Offset<ReuseEntry>(end);
            return o;
        }
    };

    inline flatbuffers::Offset <ReuseEntry> CreateReuseEntry(
            flatbuffers::FlatBufferBuilder &_fbb,
            uint64_t action = 0,
            flatbuffers::Offset <flatbuffers::Vector<flatbuffers::Offset < fastbotx::ActivityTimes>>

    >
    targets = 0
    ) {
    ReuseEntryBuilder builder_(_fbb);
    builder_.
    add_action(action);
    builder_.
    add_targets(targets);
    return builder_.

    Finish();
}

inline flatbuffers::Offset<ReuseEntry> CreateReuseEntryDirect(
        flatbuffers::FlatBufferBuilder &_fbb,
        uint64_t action = 0,
        const std::vector<flatbuffers::Offset<fastbotx::ActivityTimes>> *targets = nullptr) {
    auto targets__ = targets ? _fbb.CreateVector<flatbuffers::Offset<fastbotx::ActivityTimes>>(
            *targets) : 0;
    return fastbotx::CreateReuseEntry(
            _fbb,
            action,
            targets__);
}

struct ReuseModel FLATBUFFERS_FINAL_CLASS : private flatbuffers::Table {
    typedef ReuseModelBuilder Builder;
    enum FlatBuffersVTableOffset FLATBUFFERS_VTABLE_UNDERLYING_TYPE {
        VT_MODEL = 4
    };

    const flatbuffers::Vector<flatbuffers::Offset<fastbotx::ReuseEntry>> *model() const {
        return GetPointer<const flatbuffers::Vector<flatbuffers::Offset<fastbotx::ReuseEntry>> *>(
                VT_MODEL);
    }

    bool Verify(flatbuffers::Verifier &verifier) const {
        return VerifyTableStart(verifier) &&
               VerifyOffset(verifier, VT_MODEL) &&
               verifier.VerifyVector(model()) &&
               verifier.VerifyVectorOfTables(model()) &&
               verifier.EndTable();
    }
};

struct ReuseModelBuilder {
    typedef ReuseModel Table;
    flatbuffers::FlatBufferBuilder &fbb_;
    flatbuffers::uoffset_t start_;

    void add_model(
            flatbuffers::Offset<flatbuffers::Vector<flatbuffers::Offset<fastbotx::ReuseEntry>>> model) {
        fbb_.AddOffset(ReuseModel::VT_MODEL, model);
    }

    explicit ReuseModelBuilder(flatbuffers::FlatBufferBuilder &_fbb)
            : fbb_(_fbb) {
        start_ = fbb_.StartTable();
    }

    flatbuffers::Offset<ReuseModel> Finish() {
        const auto end = fbb_.EndTable(start_);
        auto o = flatbuffers::Offset<ReuseModel>(end);
        return o;
    }
};

inline flatbuffers::Offset<ReuseModel> CreateReuseModel(
        flatbuffers::FlatBufferBuilder &_fbb,
        flatbuffers::Offset<flatbuffers::Vector<flatbuffers::Offset<fastbotx::ReuseEntry>>> model = 0) {
    ReuseModelBuilder builder_(_fbb);
    builder_.add_model(model);
    return builder_.Finish();
}

inline flatbuffers::Offset<ReuseModel> CreateReuseModelDirect(
        flatbuffers::FlatBufferBuilder &_fbb,
        std::vector<flatbuffers::Offset<fastbotx::ReuseEntry>> *model = nullptr) {
    auto model__ = model ? _fbb.CreateVectorOfSortedTables<fastbotx::ReuseEntry>(model) : 0;
    return fastbotx::CreateReuseModel(
            _fbb,
            model__);
}

inline const fastbotx::ReuseModel *GetReuseModel(const void *buf) {
    return flatbuffers::GetRoot<fastbotx::ReuseModel>(buf);
}

inline const fastbotx::ReuseModel *GetSizePrefixedReuseModel(const void *buf) {
    return flatbuffers::GetSizePrefixedRoot<fastbotx::ReuseModel>(buf);
}

inline bool VerifyReuseModelBuffer(
        flatbuffers::Verifier &verifier) {
    return verifier.VerifyBuffer<fastbotx::ReuseModel>(nullptr);
}

inline bool VerifySizePrefixedReuseModelBuffer(
        flatbuffers::Verifier &verifier) {
    return verifier.VerifySizePrefixedBuffer<fastbotx::ReuseModel>(nullptr);
}

inline void FinishReuseModelBuffer(
        flatbuffers::FlatBufferBuilder &fbb,
        flatbuffers::Offset<fastbotx::ReuseModel> root) {
    fbb.Finish(root);
}

inline void FinishSizePrefixedReuseModelBuffer(
        flatbuffers::FlatBufferBuilder &fbb,
        flatbuffers::Offset<fastbotx::ReuseModel> root) {
    fbb.FinishSizePrefixed(root);
}

}  // namespace fastbotx

#endif  // FLATBUFFERS_GENERATED_REUSEMODELLEGACY_FASTBOTX_H_
